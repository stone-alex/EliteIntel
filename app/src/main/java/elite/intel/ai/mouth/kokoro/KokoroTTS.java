package elite.intel.ai.mouth.kokoro;

import com.google.common.eventbus.Subscribe;
import com.k2fsa.sherpa.onnx.*;
import elite.intel.ai.mouth.AudioDeClicker;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AppPaths;
import elite.intel.util.AudioPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Offline TTS using Kokoro via sherpa-onnx JNI.
 * <p>
 * ── Gradle dependency (add to app/build.gradle) ──────────────────────────────
 * implementation files('libs/sherpa-onnx-v1.12.28.jar')
 * // Download from:
 * // https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.12.28/sherpa-onnx-v1.12.28.jar
 * <p>
 * ── Native libs (vendor into src/main/resources/native/) ─────────────────────
 * linux-x86-64/libonnxruntime.so      ─┐ sherpa-onnx-v1.12.28-linux-x64-jni.tar.bz2
 * linux-x86-64/libsherpa-onnx-jni.so  ─┘
 * win-x86-64/onnxruntime.dll          ─┐ sherpa-onnx-v1.12.28-win-x64-jni.tar.bz2
 * win-x86-64/sherpa-onnx-jni.dll      ─┘
 * <p>
 * ── Model (downloaded by installer) ──────────────────────────────────────────
 * {appData}/models/tts/kokoro-en-v0_19/
 * model.onnx, voices.bin, tokens.txt, espeak-ng-data/
 * Download: https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-en-v0_19.tar.bz2
 */
public class KokoroTTS implements MouthInterface {

    private static final Logger log = LogManager.getLogger(KokoroTTS.class);

    private static final int SAMPLE_RATE = 24000;
    private static final int DEFAULT_SID = KokoroVoices.AF_HEART.getSid();
    private static final float DEFAULT_SPEED = 1.0f;

    private static volatile KokoroTTS instance;

    private final AtomicBoolean interruptRequested = new AtomicBoolean(false);
    private final AtomicBoolean canBeInterrupted = new AtomicBoolean(true);
    private final AtomicReference<SourceDataLine> currentLine = new AtomicReference<>();
    private final BlockingQueue<VocalisationRequestEvent> queue = new LinkedBlockingQueue<>();
    private final SystemSession systemSession = SystemSession.getInstance();
    private SourceDataLine persistentLine;
    private volatile boolean running = false;
    private Thread workerThread;
    private ExecutorService callbackExecutor;
    private OfflineTts tts;

    private KokoroTTS() {
    }

    public static KokoroTTS getInstance() {
        if (instance == null) {
            synchronized (KokoroTTS.class) {
                if (instance == null) instance = new KokoroTTS();
            }
        }
        return instance;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void start() {
        if (running) return;

        try {
            extractAndLoadNatives();
        } catch (Exception e) {
            log.error("KokoroTTS: native lib load failed — TTS unavailable", e);
            return;
        }

        try {
            tts = buildOfflineTts();
        } catch (Exception e) {
            log.error("KokoroTTS: engine init failed", e);
            return;
        }

        EventBusManager.register(this);
        running = true;
        queue.clear();

        callbackExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "KokoroTTS-Audio");
            t.setDaemon(true);
            return t;
        });

        workerThread = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    VocalisationRequestEvent event = queue.take();
                    callbackExecutor.submit(() -> {
                        try {
                            synthesizeAndPlay(event.getText());
                        } catch (Exception e) {
                            log.warn("KokoroTTS synthesis error: {}", e.getMessage(), e);
                        } finally {
                            EventBusManager.publish(new AppLogEvent("\n"));
                        }
                    });
                } catch (InterruptedException | RejectedExecutionException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "KokoroTTS-Worker");
        workerThread.setDaemon(false);
        workerThread.start();

        log.info("KokoroTTS started — voice: {} sid={}", KokoroVoices.AF_HEART.getDisplayName(), DEFAULT_SID);
    }

    @Override
    public void stop() {
        EventBusManager.unregister(this);
        queue.clear();
        interruptRequested.set(true);
        running = false;

        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }
        if (callbackExecutor != null) {
            callbackExecutor.shutdownNow();
            callbackExecutor = null;
        }
        if (persistentLine != null && persistentLine.isOpen()) {
            persistentLine.stop();
            persistentLine.close();
            persistentLine = null;
        }
        if (tts != null) {
            tts.release();
            tts = null;
        }
    }

    // ── MouthInterface ────────────────────────────────────────────────────────

    @Override
    public void interruptAndClear() {
        if (!canBeInterrupted.get()) return;
        queue.clear();
        interruptRequested.set(true);
        SourceDataLine line = currentLine.get();
        if (line != null && line.isOpen()) {
            line.stop();
            line.flush();
            line.start();
        }
        if (workerThread == null || !workerThread.isAlive()) {
            log.warn("KokoroTTS worker died — restarting");
            start();
        }
    }

    @Subscribe public void shutUp(TTSInterruptEvent event) {
        interruptAndClear();
    }

    @Override
    @Subscribe
    public void onVoiceProcessEvent(VocalisationRequestEvent event) {
        canBeInterrupted.set(event.canBeInterrupted());
        if (running) queue.offer(event);
    }

    // ── Synthesis ─────────────────────────────────────────────────────────────

    private void synthesizeAndPlay(String input) throws Exception {
        if (input == null || input.isBlank()) return;

        String text = sanitize(input);
        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        EventBusManager.publish(new AppLogEvent("AI: " + text + "\n"));

        GeneratedAudio audio = tts.generate(text, DEFAULT_SID, (1f + systemSession.getSpeechSpeed()));
        if (audio == null || audio.getSamples() == null || audio.getSamples().length == 0) {
            log.warn("KokoroTTS: empty audio for: {}", text);
            return;
        }

        byte[] audioData = floatToPcm16(audio.getSamples());
        AudioDeClicker.sanitize(audioData, 5);

        if (persistentLine == null || !persistentLine.isOpen()) {
            if (!openPersistentLine()) return;
        }

        currentLine.set(persistentLine);
        interruptRequested.set(false);

        AudioFormat fmt = persistentLine.getFormat();
        int frameSize = fmt.getFrameSize();

        byte[] silence = new byte[(int) (SAMPLE_RATE * 0.03f) * frameSize];
        persistentLine.write(silence, 0, silence.length);

        final int CHUNK = 8192;
        for (int offset = 0; offset < audioData.length; offset += CHUNK) {
            if (interruptRequested.get()) break;
            int remaining = audioData.length - offset;
            int thisChunk = (Math.min(CHUNK, remaining) / frameSize) * frameSize;
            if (thisChunk == 0) break;
            persistentLine.write(audioData, offset, thisChunk);
        }

        if (!interruptRequested.get()) persistentLine.drain();
        else persistentLine.flush();
    }

    private boolean openPersistentLine() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            persistentLine = (SourceDataLine) AudioSystem.getLine(info);
            persistentLine.open(format, (int) (format.getFrameSize() * format.getSampleRate()));
            persistentLine.start();
            log.info("KokoroTTS audio line: {}Hz 16-bit mono", SAMPLE_RATE);
            return true;
        } catch (Exception e) {
            log.error("KokoroTTS: failed to open audio line", e);
            return false;
        }
    }

    // ── Engine construction ───────────────────────────────────────────────────

    private OfflineTts buildOfflineTts() {
        Path modelDir = AppPaths.getTtsModelDir().resolve("kokoro-en-v0_19");
        if (!Files.exists(modelDir)) {
            throw new IllegalStateException(
                    "Kokoro model missing at: " + modelDir +
                    " — run the installer to download TTS models.");
        }

        OfflineTtsKokoroModelConfig kokoro = OfflineTtsKokoroModelConfig.builder()
                .setModel(modelDir.resolve("model.onnx").toString())
                .setVoices(modelDir.resolve("voices.bin").toString())
                .setTokens(modelDir.resolve("tokens.txt").toString())
                .setDataDir(modelDir.resolve("espeak-ng-data").toString())
                .build();

        OfflineTtsModelConfig modelConfig = OfflineTtsModelConfig.builder()
                .setKokoro(kokoro)
                .setNumThreads(2)
                .setDebug(false)
                .setProvider("cpu")
                .build();

        OfflineTtsConfig config = OfflineTtsConfig.builder()
                .setModel(modelConfig)
                .setMaxNumSentences(1)
                .build();

        return new OfflineTts(config);
    }

    // ── Native loading ────────────────────────────────────────────────────────

    private static void extractAndLoadNatives() throws IOException {
        String platform = detectPlatform();
        Path nativeDir = AppPaths.getNativeLibDir().resolve("sherpa-onnx");
        Files.createDirectories(nativeDir);

        for (String lib : nativeLibsInOrder(platform)) {
            extractAndLoad(platform, nativeDir, lib);
        }
    }

    private static String[] nativeLibsInOrder(String platform) {
        if (platform.startsWith("win")) {
            return new String[]{
                    "onnxruntime_providers_shared.dll",
                    "onnxruntime.dll",
                    "sherpa-onnx-jni.dll"
            };
        }
        // Linux (and macOS if ever needed)
        return new String[]{
                "libonnxruntime.so",
                "libsherpa-onnx-jni.so"
        };
    }

    private static void extractAndLoad(String platform, Path dir, String lib) throws IOException {
        Path target = dir.resolve(lib);
        if (!Files.exists(target)) {
            String resource = "/native/" + platform + "/" + lib;
            try (InputStream in = KokoroTTS.class.getResourceAsStream(resource)) {
                if (in == null) throw new IOException("Resource not in JAR: " + resource);
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        System.load(target.toAbsolutePath().toString());
        log.info("KokoroTTS: loaded {}", lib);
    }

    private static String detectPlatform() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) return "win-x86-64";
        if (os.contains("linux")) return "linux-x86-64";
        if (os.contains("mac")) return "osx-x86-64";
        throw new UnsupportedOperationException("Unsupported OS: " + os);
    }


    // ── Helpers ───────────────────────────────────────────────────────────────

    private static byte[] floatToPcm16(float[] samples) {
        byte[] out = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            short s = (short) (Math.max(-1f, Math.min(1f, samples[i])) * 32767);
            out[2 * i] = (byte) (s & 0xFF);
            out[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return out;
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^\\x00-\\x7F]", "")
                .replace("—", ", ")
                .replace("*", " ")
                .replace("[", "").replace("]", "")
                .replace("ETA", ". E.T.A.")
                .replace(":", " - ");
    }
}
package elite.intel.ai.mouth.kokoro;

import com.google.common.eventbus.Subscribe;
import com.k2fsa.sherpa.onnx.*;
import elite.intel.ai.mouth.AudioDeClicker;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AiResponseLogEvent;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AppPaths;
import elite.intel.util.AudioPlayer;
import elite.intel.util.StringUtls;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Offline TTS using Kokoro via sherpa-onnx JNI.
 * <p>
 * Two-queue pipeline: sentence splitting → synthesis queue → playback queue.
 * Synthesis of sentence N+1 overlaps with playback of sentence N.
 */
public class KokoroTTS implements MouthInterface {

    private static final Logger log = LogManager.getLogger(KokoroTTS.class);

    private static final int SAMPLE_RATE = 24000;
    private static final int DEFAULT_SID = KokoroVoices.HEART.getSid();

    private static volatile KokoroTTS instance;

    private final AtomicBoolean interruptRequested = new AtomicBoolean(false);
    private final AtomicBoolean canBeInterrupted = new AtomicBoolean(true);
    private final AtomicReference<SourceDataLine> currentLine = new AtomicReference<>();

    // Stage 1: raw sentence strings waiting for synthesis
    private final BlockingQueue<String> synthesisQueue = new LinkedBlockingQueue<>();
    // Stage 2: synthesized PCM waiting for playback
    private final BlockingQueue<byte[]> playbackQueue = new LinkedBlockingQueue<>();

    private final SystemSession systemSession = SystemSession.getInstance();
    private SourceDataLine persistentLine;
    private volatile boolean running = false;
    private Thread synthesisThread;
    private Thread playbackThread;
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

    // -- Lifecycle -------------------------------------------------------------

    @Override
    public void start() {
        if (running) return;
        log.info("KokoroTTS.start() called from thread: {}", Thread.currentThread().getName());
        try {
            extractAndLoadNatives();
        } catch (Exception e) {
            log.error("KokoroTTS: native lib load failed - TTS unavailable", e);
            return;
        }

        try {
            tts = buildOfflineTts();
        } catch (Exception e) {
            log.error("KokoroTTS: engine init failed", e);
            return;
        }

        running = true;
        synthesisQueue.clear();
        playbackQueue.clear();
        interruptRequested.set(false); // ← reset after stop() left it true
        EventBusManager.register(this);

        synthesisThread = new Thread(this::processSynthesisQueue, "KokoroTTS-Synthesis");
        synthesisThread.setDaemon(true);
        synthesisThread.start();

        playbackThread = new Thread(this::processPlaybackQueue, "KokoroTTS-Playback");
        playbackThread.setDaemon(false);
        playbackThread.start();

        log.info("KokoroTTS started - voice: {} sid={}", KokoroVoices.HEART.getDisplayName(), DEFAULT_SID);
    }

    @Override
    public void stop() {
        EventBusManager.unregister(this);
        running = false;
        synthesisQueue.clear();
        playbackQueue.clear();
        interruptRequested.set(true);

        if (synthesisThread != null) {
            synthesisThread.interrupt();
            try {
                synthesisThread.join(3000); // wait for in-flight generate() to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synthesisThread = null;
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
            try {
                playbackThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            playbackThread = null;
        }

        closePersistentLine();

        if (tts != null) {
            tts.release(); // safe - synthesis thread is guaranteed dead
            tts = null;
        }
    }

    // -- MouthInterface --------------------------------------------------------

    @Override
    public void interruptAndClear() {
        if (!canBeInterrupted.get()) return;

        synthesisQueue.clear();
        playbackQueue.clear();
        interruptRequested.set(true);

        SourceDataLine line = currentLine.get();
        if (line != null && line.isOpen()) {
            line.stop();
            line.flush();
            line.start();
        }
        interruptRequested.set(false);

        log.info("KokoroTTS interrupted and queues cleared");

        if (synthesisThread == null || !synthesisThread.isAlive() ||
            playbackThread == null || !playbackThread.isAlive()) {
            log.warn("KokoroTTS worker died - restarting");
            start();
        }
    }

    @Subscribe
    public void shutUp(TTSInterruptEvent event) {
        interruptAndClear();
    }

    @Override
    @Subscribe
    public void onVoiceProcessEvent(VocalisationRequestEvent event) {
        if (!running) return;
        canBeInterrupted.set(event.canBeInterrupted());

        String sanitizedText = StringUtls.sanitizeTts(event.getText());
        if (sanitizedText.isBlank()) return;

        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        EventBusManager.publish(new AiResponseLogEvent(sanitizedText));

        // Split on sentence boundaries and enqueue each piece for synthesis
        String[] sentences = sanitizedText.split("(?<=[.!?])\\s+(?=\\S)");
        for (String sentence : sentences) {
            if (!sentence.isBlank()) {
                synthesisQueue.offer(sentence);
            }
        }
    }

    // -- Stage 1: Synthesis thread ---------------------------------------------

    private void processSynthesisQueue() {
        while (running) {
            try {
                String sentence = synthesisQueue.take();
                if (interruptRequested.get()) continue;

                GeneratedAudio audio = tts.generate(
                        sentence,
                        systemSession.getKokoroVoice().getSid(),
                        1f + systemSession.getSpeechSpeed()
                );

                if (audio == null || audio.getSamples() == null || audio.getSamples().length == 0) {
                    log.warn("KokoroTTS: empty audio for: {}", sentence);
                    continue;
                }

                byte[] pcm = floatToPcm16(audio.getSamples());

                AudioDeClicker.sanitize(pcm, 5);
                //playbackQueue.put(Amplifier.amplify(pcm));
                playbackQueue.put(pcm);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("KokoroTTS synthesis error: {}", e.getMessage(), e);
            }
        }
    }

    // -- Stage 2: Playback thread ----------------------------------------------

    private void processPlaybackQueue() {
        if (!openPersistentLine()) return;

        while (running) {
            try {
                byte[] pcm = playbackQueue.poll(200, TimeUnit.MILLISECONDS);
                if (pcm == null) continue;
                if (interruptRequested.get()) continue;

                playPcm(pcm);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("KokoroTTS playback error: {}", e.getMessage(), e);
            } finally {
                EventBusManager.publish(new AppLogEvent(""));
            }
        }
        closePersistentLine();
    }

    private void playPcm(byte[] audioData) {
        if (persistentLine == null || !persistentLine.isOpen()) {
            if (!openPersistentLine()) return;
        }

        currentLine.set(persistentLine);

        AudioFormat fmt = persistentLine.getFormat();
        int frameSize = fmt.getFrameSize();

        // Small silence gap between sentences
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
            persistentLine.open(format, (int) (format.getFrameSize() * format.getSampleRate() / 10));
            persistentLine.start();
            log.info("KokoroTTS audio line: {}Hz 16-bit mono", SAMPLE_RATE);
            return true;
        } catch (Exception e) {
            log.error("KokoroTTS: failed to open audio line", e);
            return false;
        }
    }

    private void closePersistentLine() {
        if (persistentLine != null && persistentLine.isOpen()) {
            try {
                persistentLine.drain();
                persistentLine.stop();
                persistentLine.close();
            } catch (Exception e) {
                log.warn("KokoroTTS: error closing audio line", e);
            } finally {
                persistentLine = null;
            }
        }
    }

    // -- Engine construction ---------------------------------------------------

    private OfflineTts buildOfflineTts() {
        Path modelDir = AppPaths.getTtsModelDir().resolve("kokoro-en-v0_19");
        if (!Files.exists(modelDir)) {
            throw new IllegalStateException(
                    "Kokoro model missing at: " + modelDir +
                    " - run the installer to download TTS models.");
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

    // -- Native loading --------------------------------------------------------

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

    // -- Helpers ---------------------------------------------------------------

    private static byte[] floatToPcm16(float[] samples) {
        byte[] out = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            short s = (short) (Math.max(-1f, Math.min(1f, samples[i])) * 32767);
            out[2 * i] = (byte) (s & 0xFF);
            out[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return out;
    }


}
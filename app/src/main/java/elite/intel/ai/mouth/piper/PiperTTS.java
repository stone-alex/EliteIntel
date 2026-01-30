package elite.intel.ai.mouth.piper;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class PiperTTS implements MouthInterface {
    private static final Logger log = LogManager.getLogger(PiperTTS.class);
    private static volatile PiperTTS instance;
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final AtomicBoolean interruptRequested = new AtomicBoolean(false);
    private final AtomicBoolean canBeInterrupted = new AtomicBoolean(true);
    private final AtomicReference<SourceDataLine> currentLine = new AtomicReference<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final BlockingQueue<VocalisationRequestEvent> queue = new LinkedBlockingQueue<>();
    private SourceDataLine persistentLine;
    private volatile boolean running = false;
    private Thread workerThread = null;
    private ExecutorService callbackExecutor = null;

    private PiperTTS() {
        EventBusManager.register(this);
    }

    public static PiperTTS getInstance() {
        if (instance == null) {
            synchronized (PiperTTS.class) {
                if (instance == null) {
                    instance = new PiperTTS();
                }
            }
        }
        return instance;
    }

    @Override
    public void start() {
        if (running) return;

        running = true;
        queue.clear();

        callbackExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "PiperTTS-Audio");
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
                            log.warn("Failed to synthesize and play: {}", e.getMessage(), e);
                        }  finally {
                            EventBusManager.publish(new AppLogEvent("\n"));
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "PiperTTS-Worker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    @Override
    public void stop() {
        queue.clear();
        interruptRequested.set(true);
        SourceDataLine line = currentLine.get();
        if (line != null && line.isOpen()) {
            line.stop();
            line.flush();
            line.start();
            currentLine.set(line);
        }
        running = false;

        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }

        if (callbackExecutor != null) {
            callbackExecutor.shutdownNow();
            callbackExecutor = null;
        }
    }

    @Override
    public void interruptAndClear() {
        if(!canBeInterrupted.get()) return;

        queue.clear();
        interruptRequested.set(true);
        SourceDataLine line = currentLine.get();
        if (line != null && line.isOpen()) {
            line.stop();
            line.flush();
            line.start();
            currentLine.set(line);
        }
        if (workerThread == null || !workerThread.isAlive()) {
            log.warn("Processing thread stopped unexpectedly, restarting");
            start();
        }
    }

    @Subscribe public void shutUp(TTSInterruptEvent event) {
        interruptAndClear();
    }

    @Override
    public void onVoiceProcessEvent(VocalisationRequestEvent event) {
        canBeInterrupted.set(event.canBeInterrupted());
        if (running) {
            queue.offer(event);
        }
    }

    private boolean openPersistentLine() {
        try {
            AudioFormat format = new AudioFormat(22050, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            int bufferSize = (int) (format.getFrameSize() * format.getSampleRate());
            persistentLine = (SourceDataLine) AudioSystem.getLine(info);
            persistentLine.open(format, bufferSize);
            persistentLine.start();
            log.info("Piper line opened: 22050 Hz, 16-bit, mono, signed LE");
            return true;
        } catch (Exception e) {
            log.error("Failed to open Piper audio line", e);
            return false;
        }
    }

    private void synthesizeAndPlay(String input) throws Exception {
        if (input == null || input.isBlank()) return;

        //replace em-dash with comma + space
        String text = sanitize(input);

        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        EventBusManager.publish(new AppLogEvent("AI: " + text+"\n"));

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        jsonObject.addProperty("length_scale", SystemSession.getInstance().getSpeechSpeed());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(playerSession.getLocalTtsAddress()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GsonFactory.getGson().toJson(jsonObject)))
                .build();

        byte[] wavBytes = httpClient.send(request, BodyHandlers.ofByteArray()).body();  // Now full WAV
        if (wavBytes.length < 44) {
            log.warn("WAV too short: {} bytes", wavBytes.length);
            return;
        }
        byte[] audioData = java.util.Arrays.copyOfRange(wavBytes, 44, wavBytes.length);  // Raw PCM now

        if (audioData.length == 0) {
            log.warn("Empty audio from Piper");
            return;
        }

        if (persistentLine == null || !persistentLine.isOpen()) {
            if (!openPersistentLine()) return;
        }

        currentLine.set(persistentLine);
        interruptRequested.set(false);

        AudioFormat fmt = persistentLine.getFormat();
        int frameSize = fmt.getFrameSize(); // 2

        // 50ms silence to kill pop
        byte[] silence = new byte[(int) (fmt.getSampleRate() * 0.05) * frameSize];
        persistentLine.write(silence, 0, silence.length);
        final int CHUNK = 8192;
        for (int offset = 0; offset < audioData.length; offset += CHUNK) {
            if (interruptRequested.get()) break;

            int remaining = audioData.length - offset;
            int thisChunk = Math.min(CHUNK, remaining);
            thisChunk = (thisChunk / frameSize) * frameSize;
            if (thisChunk == 0) break;

            persistentLine.write(audioData, offset, thisChunk);
        }

        if (!interruptRequested.get()) {
            persistentLine.drain();
        } else {
            persistentLine.flush();
        }
    }

    private static @NonNull String sanitize(String input) {
        return input.replaceAll("[^\\x00-\\x7F]", "")
                .replace("—", ", ")
                .replace("*", " ")
                .replace("[", "")
                .replace("]", "")
                .replace("ETA", ". E.T.A.")
                ;
    }
}
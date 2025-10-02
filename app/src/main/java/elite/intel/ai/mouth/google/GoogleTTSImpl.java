package elite.intel.ai.mouth.google;

import com.google.cloud.texttospeech.v1.*;
import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ConfigManager;
import elite.intel.ai.mouth.*;
import elite.intel.ai.mouth.subscribers.events.*;
import elite.intel.gameapi.EventBusManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.sound.sampled.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of {@code MouthInterface} for Google Text-to-Speech (TTS).
 * Provides functionality for text-to-speech synthesis using Google Cloud TTS APIs.
 * This class is responsible for managing the lifecycle of TTS operations,
 * queuing requests, and handling event-driven vocalization.
 */
public class GoogleTTSImpl implements MouthInterface {
    private static final Logger log = LogManager.getLogger(GoogleTTSImpl.class);

    private TextToSpeechClient textToSpeechClient;
    private final BlockingQueue<VoiceRequest> voiceQueue;
    private Thread processingThread;
    private volatile boolean running;
    private final GoogleVoiceProvider googleVoiceProvider;
    private final AtomicBoolean interruptRequested = new AtomicBoolean(false);
    private final AtomicReference<SourceDataLine> currentLine = new AtomicReference<>();

    private static final GoogleTTSImpl INSTANCE = new GoogleTTSImpl();

    public static GoogleTTSImpl getInstance() {
        return INSTANCE;
    }

    private record VoiceRequest(String text, String voiceName, double speechRate, Class<? extends BaseVoxEvent> originType) {
    }

    private GoogleTTSImpl() {
        EventBusManager.register(this);
        this.voiceQueue = new LinkedBlockingQueue<>();
        googleVoiceProvider = GoogleVoiceProvider.getInstance();
    }

    @Override
    public synchronized void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("VoiceGenerator is already running");
            return;
        }

        try {
            String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.TTS_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("TTS API key not found in system.conf");
                return;
            }
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setApiKey(apiKey).build();
            textToSpeechClient = TextToSpeechClient.create(settings);
            log.info("TextToSpeechClient initialized successfully with API key");
        } catch (Exception e) {
            log.error("Failed to initialize TextToSpeechClient: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize TextToSpeechClient", e);
        }

        running = true;
        processingThread = new Thread(this::processVoiceQueue, "VoiceGeneratorThread");
        processingThread.start();
        log.info("VoiceGenerator started");
    }

    @Override
    public synchronized void stop() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("VoiceGenerator is not running");
            return;
        }
        running = false;
        interruptAndClear();
        processingThread.interrupt();
        try {
            processingThread.join(5000);
            log.info("VoiceGenerator stopped");
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for VoiceGenerator to stop", e);
            Thread.currentThread().interrupt();
        }
        try {
            if (textToSpeechClient != null) {
                textToSpeechClient.close();
                log.info("TextToSpeechClient closed");
            }
        } catch (Exception e) {
            log.error("Failed to close TextToSpeechClient", e);
        }
        processingThread = null;
        textToSpeechClient = null;
    }

    /**
     * Interrupts any ongoing voice synthesis operation, clears the voice request queue,
     * and resets the state of the text-to-speech (TTS) system to allow for future operations.
     *<p>
     * This method ensures the following:
     * - Any pending requests in the voice queue are cleared.
     * - The `interruptRequested` flag is set to prevent further processing of requests momentarily.
     * - The currently active audio playback `SourceDataLine` (if any) is stopped, closed, and removed.
     * - The `interruptRequested` flag is reset to indicate that the system is ready for new voice requests.
     * - Logs the interruption and state of the system.
     * - Verifies if the processing thread is running; if it has stopped unexpectedly, the method restarts the thread.
     *<p>
     * This method is synchronized to ensure thread-safe modifications to shared resources.
     */
    @Override
    public synchronized void interruptAndClear() {
        voiceQueue.clear();
        interruptRequested.set(true);
        SourceDataLine line = currentLine.get();
        if (line != null) {
            line.stop();
            line.close();
            currentLine.set(null);
        }
        interruptRequested.set(false); // Reset flag to allow new vocalizations
        log.info("TTS interrupted and queue cleared, thread alive={}, interruptRequested={}",
                processingThread != null && processingThread.isAlive(), interruptRequested.get());
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("Processing thread stopped unexpectedly, restarting");
            start();
        }
    }

    @Subscribe
    public void onInterruptEvent(TTSInterruptEvent event) {
        interruptAndClear();
    }

    @Subscribe
    @Override
    public void onVoiceProcessEvent(VocalisationRequestEvent event) {
        log.debug("Received VoiceProcessEvent: text='{}', useRandom={}", event.getText(), event.useRandomVoice());
        if (event.getText() == null || event.getText().isEmpty()) {
            return;
        }
        try {
            String voiceName = event.useRandomVoice()
                    ? googleVoiceProvider.getRandomVoice().getName()
                    : googleVoiceProvider.getUserSelectedVoice().getName();
            voiceQueue.put(new VoiceRequest(event.getText(), voiceName, googleVoiceProvider.getSpeechRate(voiceName), event.getOriginType()));
            log.debug("Added VoiceRequest to queue: text='{}', voice='{}'", event.getText(), voiceName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while adding voice request to queue");
        }
    }

    private void processVoiceQueue() {
        while (running) {
            try {
                log.trace("Polling voice queue, size={}, interruptRequested={}",
                        voiceQueue.size(), interruptRequested.get());
                VoiceRequest request = voiceQueue.poll(1, TimeUnit.SECONDS);
                if (request == null) {
                    if (Thread.currentThread().isInterrupted() || !running) {
                        log.info("Shutting down VoiceGenerator due to interruption or stop signal");
                        return;
                    }
                    continue;
                }
                processVoiceRequest(
                        request.text().replace("present","detected"),
                        request.voiceName(),
                        request.speechRate(),
                        request.originType()
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("VoiceGenerator interrupted, shutting down");
                return;
            } catch (Exception e) {
                log.error("Unexpected error in VoiceGenerator", e);
            }
        }
    }

    private void processVoiceRequest(String text, String voiceName, double speechRate, Class<? extends BaseVoxEvent> originType) {
        if (text == null || text.isEmpty()) {
            return;
        }
        long startTime = System.currentTimeMillis();
        Thread currentThread = Thread.currentThread();
        log.debug("Processing VoiceRequest: text='{}', voice='{}', threadName='{}', threadState={}", text, voiceName, currentThread.getName(), currentThread.getState());

        try {
            if (textToSpeechClient == null) {
                log.error("TextToSpeechClient is null, restarting service");
                synchronized (this) {
                    start();
                }
                if (textToSpeechClient == null) {
                    log.error("Failed to reinitialize TextToSpeechClient, skipping request");
                    return;
                }
            }

            VoiceSelectionParams voice = googleVoiceProvider.getVoiceParams(voiceName);
            if (voice == null) {
                log.warn("No voice found for name: {}, using default", voiceName);
                voice = googleVoiceProvider.getVoiceParams(AiVoices.JENNIFER.getName());
            }

            if (interruptRequested.get()) {
                log.debug("Request interrupted before synthesis, skipping: {}", text);
                return;
            }

            log.debug("Calling Google TTS API");
            long apiStartTime = System.currentTimeMillis();
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            AudioConfig config = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(speechRate)
                    .build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, config);
            long apiEndTime = System.currentTimeMillis();
            log.debug("Google TTS API call completed in {}ms", apiEndTime - apiStartTime);

            byte[] audioData = response.getAudioContent().toByteArray();

            AudioDeClicker.sanitize(audioData, 40); // removes clicks and applies fade in and fade out

            log.debug("Opening SourceDataLine");
            long lineOpenStartTime = System.currentTimeMillis();
            AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            int bufferBytes = (int) (format.getFrameSize() * format.getSampleRate() / 10);
            int silenceFrames = (int) (format.getSampleRate() / 50);
            byte[] silenceBuffer = new byte[silenceFrames * format.getFrameSize()];

            currentLine.set(null);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format, bufferBytes);
                currentLine.set(line);
                log.debug("SourceDataLine opened in {}ms", System.currentTimeMillis() - lineOpenStartTime);

                log.debug("Starting playback");
                line.write(silenceBuffer, 0, silenceBuffer.length);
                line.start();
                log.info("Spoke with voice {}: {}", voiceName, text);

                long writeStartTime = System.currentTimeMillis();
                for (int i = 0; i < audioData.length; i += bufferBytes) {
                    if (interruptRequested.get()) {
                        log.debug("Playback interrupted mid-stream: {}", text);
                        break;
                    }
                    int len = Math.min(bufferBytes, audioData.length - i);
                    line.write(audioData, i, len);
                }
                if (interruptRequested.get()) {
                    line.flush();
                    log.debug("Playback interrupted");
                } else {
                    line.drain();
                }
                publishCompletionEvent(originType);
                
                log.debug("Audio playback completed in {}ms", System.currentTimeMillis() - writeStartTime);
            } catch (LineUnavailableException e) {
                log.error("Audio device unavailable, possible contention: {}", e.getMessage(), e);
                try {
                    log.debug("Retrying SourceDataLine open");
                    SourceDataLine retryLine = (SourceDataLine) AudioSystem.getLine(info);
                    retryLine.open(format, bufferBytes);
                    currentLine.set(retryLine);
                    retryLine.write(silenceBuffer, 0, silenceBuffer.length);
                    retryLine.start();
                    for (int i = 0; i < audioData.length; i += bufferBytes) {
                        if (interruptRequested.get()) {
                            log.debug("Playback interrupted mid-stream on retry: {}", text);
                            break;
                        }
                        int len = Math.min(bufferBytes, audioData.length - i);
                        retryLine.write(audioData, i, len);
                    }
                    if (interruptRequested.get()) {
                        retryLine.flush();
                    } else {
                        retryLine.drain();
                    }
                    retryLine.close();
                    publishCompletionEvent(originType);
                    log.info("Audio playback retried successfully");
                } catch (LineUnavailableException retryEx) {
                    log.error("Retry failed for audio device: {}", retryEx.getMessage(), retryEx);
                }
            }
        } catch (Exception e) {
            log.error("Text-to-speech error: {}", e.getMessage(), e);
        } finally {
            currentLine.set(null);
            interruptRequested.set(false);
        }
        log.debug("VoiceRequest processing completed in {}ms", System.currentTimeMillis() - startTime);
    }

    private void publishCompletionEvent(Class<? extends BaseVoxEvent> originType) {
        try {
            EventBusManager.publish(
                    new VocalisationSuccessfulEvent<>(
                            originType.getConstructor(String.class).newInstance("")
                    )
            );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to publish VocalisationSuccessfulEvent {}", e.getMessage(), e);
        }
    }
}
package elite.intel.ai.mouth.google;

import com.google.cloud.texttospeech.v1.*;
import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ConfigManager;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of the {@code MouthInterface} that uses the Google Text-to-Speech API
 * to convert text into synthesized speech. The class maintains a queue of text-to-speech
 * requests to be processed sequentially. It is implemented as a singleton to ensure
 * only one instance is active during execution.
 *
 * This class works by managing a background thread (`VoiceGeneratorThread`) to continuously
 * process the queue of text-to-speech requests. Each request is handled asynchronously
 * with speech synthesis conducted via the Google Text-to-Speech API.
 *
 * Features include:
 * - Configurable speech synthesis parameters through {@link VoiceRequest}, such as text, voice type, and speech rate.
 * - Support for interruption and clearing of the voice request queue.
 * - Integration with a Google Voice provider to fetch voice configurations dynamically.
 * - Event subscription to handle voice processing and interruption triggers.
 *
 * Thread safety:
 * - Synchronization is applied to methods that start, stop, or interrupt the processing thread.
 * - Atomic variables are used to manage critical flags for thread-safe operation.
 *
 * Usage scenario:
 * - This class can be used in applications requiring server-side or client-side text-to-speech synthesis
 *   to provide auditory feedback.
 *
 * Dependencies:
 * - Google Cloud TextToSpeechClient for handling API interactions.
 * - EventBus for subscribing to event-based requests such as `VoiceProcessEvent` and `TTSInterruptEvent`.
 * - Java's {@code javax.sound.sampled.SourceDataLine} for audio playback.
 * - Configuration support for Google API system key.
 * - Threading utilities including {@code Thread}, {@code BlockingQueue}, and {@code AtomicReference}.
 */
public class GoogleTTSImpl implements MouthInterface {
    private static final Logger log = LoggerFactory.getLogger(GoogleTTSImpl.class);

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

    private record VoiceRequest(String text, String voiceName, double speechRate) {
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
     * Interrupts any ongoing text-to-speech (TTS) processing, clears the voice queue, and resets the state.
     *
     * This method is synchronized to ensure thread safety during operations that may involve concurrent
     * access or modification of shared resources such as the voice queue and audio processing components.
     *
     * The method performs the following actions:
     * 1. Clears the voice queue to remove any pending TTS requests.
     * 2. Sets the interrupt flag to indicate that the current process should be stopped.
     * 3. If an audio line is actively playing, stops and closes it, releasing its resources.
     * 4. Resets the interrupt flag after completing the interruption process.
     * 5. Logs the current state of the TTS service and checks if the processing thread is alive.
     *    If the thread is no longer active, it logs a warning and restarts the TTS service.
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
    public void onVoiceProcessEvent(VoiceProcessEvent event) {
        log.debug("Received VoiceProcessEvent: text='{}', useRandom={}", event.getText(), event.isUseRandom());
        if (event.isUseRandom()) {
            speak(event.getText(), googleVoiceProvider.getRandomVoice());
        } else {
            speak(event.getText(), googleVoiceProvider.getUserSelectedVoice());
        }
    }

    private void speak(String text, AiVoices aiVoice) {
        if (text == null || text.isEmpty()) {
            return;
        }
        try {
            voiceQueue.put(new VoiceRequest(text, aiVoice.getName(), aiVoice.getSpeechRate()));
            log.debug("Added VoiceRequest to queue: text='{}', voice='{}'", text, aiVoice.getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while adding voice request to queue", e);
        }
    }

    /**
     * Processes the voice request queue by continuously polling for new {@link VoiceRequest} objects,
     * handling interruptions, and calling the appropriate methods to perform text-to-speech synthesis
     * and audio playback.
     *
     * The method runs in a loop as long as the `running` flag is true and performs the following steps:
     *
     * - Polls the `voiceQueue` to retrieve the next voice request. If no request is available within
     *   the defined timeout, it checks for interruption or stop signals before continuing.
     * - When a valid {@link VoiceRequest} is retrieved, it extracts the text, voice name, and speech
     *   rate from the request and passes them to {@link #processVoiceRequest(String, String, double)}
     *   for further processing.
     * - Handles interruptions by setting the current thread's interrupt status and exiting the loop.
     * - Logs relevant details, including the queue status, interruptions, and unexpected errors.
     *
     * This method provides thread-safe processing of text-to-speech requests and ensures proper
     * shutdown in cases of interruption or stop signals.
     *
     * Throws:
     * - InterruptedException: If the thread is interrupted during the queue polling.
     * - Exception: For any unexpected issues during the process.
     */
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
                processVoiceRequest(request.text(), request.voiceName(), request.speechRate());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("VoiceGenerator interrupted, shutting down");
                return;
            } catch (Exception e) {
                log.error("Unexpected error in VoiceGenerator", e);
            }
        }
    }

    /**
     * Processes a text-to-speech request by synthesizing speech from the provided text
     * using the specified voice and speech rate and plays the resulting audio.
     *
     * @param text the text content to synthesize into speech.
     *             If null or empty, the method will return without any action.
     * @param voiceName the name of the voice to use during synthesis.
     *                  If the specified voice cannot be found, a default voice will be used.
     * @param speechRate the rate of speech during audio playback.
     *                   Values typically range from 0.25 (slowest) to 4.0 (fastest),
     *                   with 1.0 being the normal rate.
     */
    private void processVoiceRequest(String text, String voiceName, double speechRate) {
        if (text == null || text.isEmpty()) {
            return;
        }
        long startTime = System.currentTimeMillis();
        Thread currentThread = Thread.currentThread();
        log.debug("Processing VoiceRequest: text='{}', voice='{}', threadName='{}', threadId={}, threadState={}",
                text, voiceName, currentThread.getName(), currentThread.getId(), currentThread.getState());

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
                    .setVolumeGainDb(-3)
                    .build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, config);
            long apiEndTime = System.currentTimeMillis();
            log.debug("Google TTS API call completed in {}ms", apiEndTime - apiStartTime);

            byte[] audioData = response.getAudioContent().toByteArray();
            if ((audioData.length & 1) != 0) {
                byte[] even = new byte[audioData.length - 1];
                System.arraycopy(audioData, 0, even, 0, even.length);
                audioData = even;
            }
            applyFade(audioData, 20, true);
            applyFade(audioData, 20, false);

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

    private void applyFade(byte[] audioData, int fadeMs, boolean isFadeIn) {
        int samplesToFade = (24000 * fadeMs) / 1000;
        int startIndex = isFadeIn ? 0 : Math.max(0, audioData.length / 2 - samplesToFade);
        for (int i = startIndex; i < startIndex + samplesToFade && (i * 2 + 1) < audioData.length; i++) {
            int lo = audioData[2 * i] & 0xFF;
            int hi = audioData[2 * i + 1] & 0xFF;
            short sample = (short) ((hi << 8) | lo);
            float gain = isFadeIn ? (float) i / samplesToFade : (float) (startIndex + samplesToFade - i) / samplesToFade;
            int scaled = Math.round(sample * gain);
            audioData[2 * i] = (byte) (scaled & 0xFF);
            audioData[2 * i + 1] = (byte) ((scaled >>> 8) & 0xFF);
        }
    }
}
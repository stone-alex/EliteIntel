package elite.companion.ai.mouth.google;

import com.google.cloud.texttospeech.v1.*;
import com.google.common.eventbus.Subscribe;
import elite.companion.ai.ConfigManager;
import elite.companion.ai.mouth.AiVoices;
import elite.companion.ai.mouth.MouthInterface;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the MouthInterface that utilizes Google Cloud Text-to-Speech API
 * to generate human-like speech audio from text. This class contains methods for initializing
 * and managing the Text-to-Speech client, processing voice requests, and handling events
 * related to text-to-speech actions.
 *
 * <p>The class maintains a queue system for handling multiple voice requests asynchronously.
 * It also supports selecting different voices for speech synthesis and provides functionality
 * for managing the lifecycle of the text-to-speech processing thread.
 * <p>
 * This class implements the Singleton design pattern, allowing one instance to manage
 * all text-to-speech operations within the system.
 */
public class GoogleTTSImpl implements MouthInterface {
    private static final Logger log = LoggerFactory.getLogger(GoogleTTSImpl.class);

    private TextToSpeechClient textToSpeechClient;
    private final BlockingQueue<VoiceRequest> voiceQueue;
    private Thread processingThread;
    private volatile boolean running;
    private final GoogleVoiceProvider googleVoiceProvider;

    private static final GoogleTTSImpl INSTANCE = new GoogleTTSImpl();

    public static GoogleTTSImpl getInstance() {
        return INSTANCE;
    }

    private static class VoiceRequest {
        final String text;
        final String voiceName;
        final double speechRate;

        VoiceRequest(String text, String voiceName, double speechRate) {
            this.text = text;
            this.voiceName = voiceName;
            this.speechRate = speechRate;
        }
    }

    private GoogleTTSImpl() {
        EventBusManager.register(this);
        this.voiceQueue = new LinkedBlockingQueue<>();
        googleVoiceProvider = GoogleVoiceProvider.getInstance();
    }

    /**
     * Starts the VoiceGenerator service. This method initializes the TextToSpeechClient
     * using the API key from the system configuration, sets up the required resources,
     * and starts a separate processing thread to handle the voice generation tasks.
     * If the service is already running, the start operation is skipped with
     * a warning log.
     * <p>
     * Throws:
     * - RuntimeException if the TextToSpeechClient initialization fails.
     * <p>
     * Ensures:
     * - The TextToSpeechClient is successfully created and ready for use.
     * - A dedicated thread (VoiceGeneratorThread) is launched to process the
     * voice requests from the queue.
     */
    @Override
    public synchronized void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("VoiceGenerator is already running");
            return;
        }

        TextToSpeechClient client;
        try {
            String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.TTS_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("TTS API key not found in system.conf");
                return;
            }
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setApiKey(apiKey).build();
            client = TextToSpeechClient.create(settings);
            log.info("TextToSpeechClient initialized successfully with API key");
        } catch (Exception e) {
            log.error("Failed to initialize TextToSpeechClient: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize TextToSpeechClient", e);
        }
        this.textToSpeechClient = client;

        running = true;
        processingThread = new Thread(this::processVoiceQueue, "VoiceGeneratorThread");
        processingThread.start();
        log.info("VoiceGenerator started");
    }

    /**
     * Stops the VoiceGenerator service if it is currently running. This method ensures that the
     * service is cleanly terminated by interrupting the processing thread, waiting for it to
     * finish, and releasing resources like the TextToSpeechClient.
     * <p>
     * Behavior:
     * - If the service is not running, logs a warning and exits without performing any actions.
     * - If the service is running:
     * - Sets the running flag to false to signal termination.
     * - Interrupts the processing thread and waits for it to stop, with a timeout of 5 seconds.
     * - Logs an error if interrupted while waiting for the thread to stop and restores the
     * interrupted status.
     * - Attempts to close the TextToSpeechClient and logs an error if this operation fails.
     * <p>
     * Postconditions:
     * - The processing thread is stopped and set to null.
     * - The TextToSpeechClient is closed, releasing its resources.
     * <p>
     * Thread Safety:
     * - This method is synchronized to prevent concurrent modifications while stopping the service.
     * <p>
     * Logging:
     * - Logs warnings, informational messages, and errors as appropriate during the stop process.
     */
    @Override
    public synchronized void stop() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("VoiceGenerator is not running");
            return;
        }
        running = false;
        processingThread.interrupt();
        try {
            processingThread.join(5000); // Wait up to 5 seconds for clean shutdown
            log.info("VoiceGenerator stopped");
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for VoiceGenerator to stop", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        try {
            textToSpeechClient.close();
            log.info("TextToSpeechClient closed");
        } catch (Exception e) {
            log.error("Failed to close TextToSpeechClient", e);
        }
        processingThread = null;
    }

    /**
     * Handles the VoiceProcessEvent to generate speech based on the event's configuration.
     * If the event specifies random voice usage, a random voice is selected for speech synthesis.
     * Otherwise, the user-selected voice is used.
     *
     * @param event the VoiceProcessEvent containing the text to synthesize and the voice configuration.
     */
    @Subscribe
    @Override
    public void onVoiceProcessEvent(VoiceProcessEvent event) {
        if (event.isUseRandom()) {
            speak(event.getText(), googleVoiceProvider.getRandomVoice());
        } else {
            speak(event.getText(), googleVoiceProvider.getUserSelectedVoice());
        }
    }

    /**
     * Adds a voice synthesis request to the processing queue if the provided text is not null or empty.
     * The request includes the text to be spoken, the selected voice, and its speech rate.
     *
     * @param text the text to be synthesized into speech, must not be null or empty
     * @param aiVoice the voice configuration used for speech synthesis including name and speech rate
     */
    private void speak(String text, AiVoices aiVoice) {
        if (text == null || text.isEmpty()) {
            return;
        }
        try {
            voiceQueue.put(new VoiceRequest(text, aiVoice.getName(), aiVoice.getSpeechRate()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while adding voice request to queue", e);
        }
    }

    /**
     * Continuously processes requests from the voice generation queue while the service is running.
     * This method retrieves voice synthesis tasks from the queue, processes them, and handles any
     * interruption or errors that occur during execution.
     *
     * Behavior:
     * - Polls the `voiceQueue` for voice synthesis requests with a timeout of 1 second.
     * - If a request is retrieved, it invokes the `processVoiceRequest` method to handle the synthesis.
     * - If the queue is empty after the timeout, it checks if the thread is interrupted or the service
     *   has been stopped, and exits if required.
     * - Logs shutdown messages when interrupted or stopped.
     * - Catches and logs unexpected errors during processing without halting the service.
     *
     * Threading:
     * - Designed to run in a dedicated thread to continuously process the voice queue.
     *
     * Exception Handling:
     * - If interrupted, the thread's interrupted status is restored with `Thread.currentThread().interrupt()`
     *   and the service shuts down gracefully.
     * - Logs any unexpected exceptions without disrupting queue processing.
     *
     * Prerequisites:
     * - The `voiceQueue` must be properly initialized before invoking this method.
     * - The `processVoiceRequest` method must be implemented for handling individual synthesis requests.
     *
     * Postconditions:
     * - The method terminates when the `running` flag is set to false or the thread is interrupted.
     * - Ensures graceful shutdown by logging interruptions and maintaining service integrity.
     */
    private void processVoiceQueue() {
        while (running) {
            try {
                VoiceRequest request = voiceQueue.poll(1, TimeUnit.SECONDS);
                if (request == null) {
                    if (Thread.currentThread().isInterrupted() || !running) {
                        log.info("Shutting down VoiceGenerator due to interruption or stop signal");
                        return;
                    }
                    continue;
                }
                processVoiceRequest(request.text, request.voiceName, request.speechRate);
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
     * Processes a voice synthesis request by converting the provided text into speech
     * using the specified voice and speech rate. The synthesized audio is then played
     * directly using the Java Sound API. If the voice name is not found, a default voice
     * is used for synthesis. Ensures audio is prepared and played with minimal distortion
     * using various audio processing techniques, such as fade-in and fade-out.
     *
     * @param text       the text to be synthesized into speech; must not be null or empty
     * @param voiceName  the name of the voice to use for speech synthesis
     * @param speechRate the rate at which the speech is synthesized; greater values imply faster speech
     */
    private void processVoiceRequest(String text, String voiceName, double speechRate) {
        if (text == null || text.isEmpty()) {
            return;
        }
        log.info("Speaking with voice {}: {}", voiceName, text);
        try {
            VoiceSelectionParams voice = googleVoiceProvider.getVoiceParams(voiceName);
            if (voice == null) {
                log.warn("No voice found for name: {}, using default", voiceName);
                voice = googleVoiceProvider.getVoiceParams(AiVoices.JENNIFER.getName());
            }

            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            AudioConfig config = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(speechRate)
                    .setSampleRateHertz(24000)
                    .build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, config);
            byte[] audioData = response.getAudioContent().toByteArray();

            // Ensure 16-bit alignment (avoid partial sample pop)
            if ((audioData.length & 1) != 0) {
                byte[] even = new byte[audioData.length - 1];
                System.arraycopy(audioData, 0, even, 0, even.length);
                audioData = even;
            }

            // Apply a short linear fade-in and fade out (~20 ms) to avoid initial discontinuity
            applyFade(audioData, 20, true);
            applyFade(audioData, 20, false);

            // Play audio directly using Java Sound
            AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                int bufferBytes = (int) (format.getFrameSize() * format.getSampleRate() / 10); // ~100ms
                line.open(format, bufferBytes);
                int silenceFrames = (int) (format.getSampleRate() / 50); // 20 ms
                byte[] silenceBuffer = new byte[silenceFrames * format.getFrameSize()];
                line.write(silenceBuffer, 0, silenceBuffer.length); // Prime the line
                line.start();
                line.write(audioData, 0, audioData.length);
                line.drain();
                line.stop();
            }
        } catch (Exception e) {
            log.error("Text-to-speech error: {}", e.getMessage(), e);
        }
    }

    /**
     * Applies a fade-in or fade-out effect to the given audio data.
     * This method modifies the raw audio data in place to gradually change the volume
     * over the specified duration, starting from or ending at full volume, depending
     * on the type of fade effect requested.
     *
     * @param audioData the array of raw audio data in 24kHz mono format, where each sample
     *                  is represented by two consecutive bytes (little-endian format)
     * @param fadeMs    the duration of the fade effect in milliseconds
     * @param isFadeIn  a boolean indicating the type of fade effect;
     *                  true for fade-in (volume increases over time), false for fade-out (volume decreases over time)
     */
    private static void applyFade(byte[] audioData, int fadeMs, boolean isFadeIn) {
        int samplesToFade = (24000 * fadeMs) / 1000; // 24kHz mono
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
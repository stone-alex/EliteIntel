package elite.companion.ai.ears.google;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import elite.companion.ai.ApiFactory;
import elite.companion.ai.ConfigManager;
import elite.companion.ai.brain.AiCommandInterface;
import elite.companion.ai.brain.AiRequestHints;
import elite.companion.ai.ears.EarsInterface;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.UserInputEvent;
import elite.companion.session.SystemSession;
import elite.companion.ui.event.AppLogEvent;
import elite.companion.util.AudioFormatDetector;
import elite.companion.util.AudioSettingsTuple;
import elite.companion.util.DaftSecretarySanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements Google Speech-to-Text (STT) functionality with real-time audio processing,
 * transcription, and integration with AI command systems through the `EarsInterface`.
 * This class uses Google Cloud Speech-to-Text API to convert audio input into textual
 * transcriptions while providing voice activity detection, audio stream management, and
 * recognition configuration customization.
 *
 * Core Responsibilities:
 * - Handles initialization and management of Google Speech Client for streaming audio transcription.
 * - Integrates with AI systems to process recognized speech commands.
 * - Manages voice activity detection (VAD) to optimize streaming operations.
 * - Provides thread-safe handling of audio input, transcription queuing, and session lifecycle.
 *
 * Key Features:
 * - Automatic handling of Google STT API configuration, including language and contextual boosting.
 * - Detection of speech and silence using RMS thresholds for efficient resource utilization.
 * - Support for audio buffer management and system configurations for high-quality recognition.
 *
 * Fields:
 * - log: Logger utility for capturing runtime states, errors, and debugging information.
 * - sampleRateHertz, bufferSize, CHANNELS: Audio configuration parameters for stream processing.
 * - RESTART_DELAY_MS, STREAM_DURATION_MS, KEEP_ALIVE_INTERVAL_MS: Timing configurations for streaming sessions.
 * - isListening, isActive: Flags to monitor the state and activity of the recognition process.
 * - wavFile: Optional file used for debugging and recording live audio input.
 * - transcriptionQueue: Queue to store transcriptions for further processing.
 * - speechClient: Instance of Google Cloud Speech API client for managing STT operations.
 * - aiCommandInterface: Interface to handle recognized commands and delegate to AI systems for processing.
 * - RMS_THRESHOLD_HIGH, RMS_THRESHOLD_LOW: Threshold values for detecting voice and silence.
 * - lastAudioSentTime, lastBuffer: Metadata for the last processed audio segment.
 * - consecutiveVoice, consecutiveSilence: Counters for voice activity detection transitions.
 * - audioInputStream: Input audio stream for processing by the recognition system.
 * - processingThread: Thread responsible for running the audio processing and recognition pipeline.
 *
 * Threading:
 * - Manages multiple threads for audio capture, real-time streaming, and processing.
 * - Ensures proper synchronization between audio input, transcription retrieval, and AI communication.
 *
 * Exceptions:
 * - Logs and handles runtime errors during initialization, audio processing, and API communication.
 * - Ensures proper resource cleanup to handle failures gracefully.
 */
public class GoogleSTTImpl implements EarsInterface {
    private static final Logger log = LoggerFactory.getLogger(GoogleSTTImpl.class);

    private int sampleRateHertz;  // Dynamically detected
    private int bufferSize; // Dynamically calculated based on sample rate


    private static final int CHANNELS = 1; // Mono
    private static final int RESTART_DELAY_MS = 50; // 50ms sleep after stream close
    private File wavFile = null; // Output WAV for debugging
    private final BlockingQueue<String> transcriptionQueue = new LinkedBlockingQueue<>();
    private SpeechClient speechClient;
    private final AiCommandInterface aiCommandInterface;
    private final AtomicBoolean isListening = new AtomicBoolean(true);
    private long lastAudioSentTime = System.currentTimeMillis();
    private byte[] lastBuffer = null;
    private AudioInputStream audioInputStream = null;

    private static final double RMS_THRESHOLD_HIGH = 250.0; // Configurable: Enter active on voice (calibrate: 200-1500 typical)
    private static final double RMS_THRESHOLD_LOW = 20.0; // Configurable: Exit active on sustained silence (calibrate: 10-100 typical)
    private static final int ENTER_VOICE_FRAMES = 1; // Quick enter to avoid initial clipping (100ms buffer = ~0.1s delay max)
    private static final int EXIT_SILENCE_FRAMES = 20; // ~2s silence to exit (handles pauses in speech)
    private static final int STREAM_DURATION_MS = 300000; // 5 min; matches Google V1 limit
    private static final int KEEP_ALIVE_INTERVAL_MS = 3000; // Increase to 5s to minimize silence sends (5000 is a good number)

    // New state trackers
    private boolean isActive = false;
    private int consecutiveVoice = 0;
    private int consecutiveSilence = 0;

    private Thread processingThread;

    @Override public void stop() {
        shutdown();
        this.processingThread.interrupt();
    }

    /**
     * Starts the speech recognition process by initializing necessary resources,
     * configuring audio input settings, and launching the processing thread.
     * <p>
     * This method ensures that only one active recognition session runs at any time.
     * It detects and sets supported audio formats, initializes the Google SpeechClient
     * with an API key, and starts a background thread for handling audio streaming
     * and transcription.
     * <p>
     * Logs warnings and errors to indicate the state of initialization and operation.
     * Handles exceptions occurring during SpeechClient setup and processing thread creation.
     * <p>
     * Preconditions:
     * - Requires the API key for Google Speech-to-Text to be configured in the system.
     * - Relies on external dependencies, including `AudioFormatDetector` and `ConfigManager`,
     * to fetch audio settings and system configurations.
     * <p>
     * Threading:
     * - Operates asynchronously in a dedicated background thread for audio streaming.
     * <p>
     * Failures:
     * - Logs and throws runtime exceptions if the initialization of core components fails.
     * - Returns silently if a speech recognition process is already active.
     */
    @Override public void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("Speech recognition is already running");
            return;
        }

        AudioSettingsTuple<Integer, Integer> formatResult = AudioFormatDetector.detectSupportedFormat();
        this.sampleRateHertz = formatResult.getSampleRate();
        this.bufferSize = formatResult.getBufferSize();

        try {
            String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.STT_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("STT API key not found in system.conf");
                return;
            }

            SpeechSettings settings = SpeechSettings.newBuilder()
                    .setApiKey(apiKey)
                    .build();
            this.speechClient = SpeechClient.create(settings);
            log.info("SpeechClient initialized successfully with API key");
        } catch (Exception e) {
            log.error("Failed to initialize SpeechClient: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize SpeechClient", e);
        }

        isListening.set(true);
        this.processingThread = new Thread(this::startStreaming);
        this.processingThread.start();
        try {
            this.aiCommandInterface.start();
            log.info("SpeechRecognizer started in background thread");
        } catch (Exception e) {
            log.error("Failed to initialize Grok", e);
            throw new RuntimeException(e);
        }
    }

    public GoogleSTTImpl() {
        this.aiCommandInterface = ApiFactory.getInstance().getCommandEndpoint();
    }

    /**
     * Initiates a streaming audio session for Google Speech-to-Text (STT) recognition.
     * This method repeatedly attempts to capture audio from the system microphone, process it in real-time,
     * and send it to the Google Speech API for transcription. It manages voice activity detection (VAD),
     * keep-alive packets, and session reinitialization upon completion or failure.
     * <p>
     * Key Functions:
     * - Configures the audio input format and initializes audio capture through `TargetDataLine`.
     * - Creates and manages an `ApiStreamObserver` for bidirectional streaming communication with the Google Speech API.
     * - Sends initial configuration and buffered audio data if available at the start of streaming.
     * - Reads audio data from the microphone, processes it, and performs voice activity detection to determine whether
     * to send audio blocks or silence-based keep-alive packets.
     * - Manages streaming session duration and reinitializes upon session end or error.
     * <p>
     * Logs:
     * - Logs audio session states, including initialization, audio format details, and VAD transitions.
     * - Provides debug logs for active audio packets and keep-alive transmissions.
     * - Captures and logs errors, including audio capture issues and streaming session failures.
     * <p>
     * Threading:
     * Operates within a loop that respects the `isListening` flag, ensuring the session halts cleanly
     * when directed to stop. Uses thread priorities for performance optimization. Implements delays
     * between session restarts to avoid overloading resources.
     * <p>
     * Exceptions:
     * - Handles `LineUnavailableException`, `IllegalArgumentException`, and other runtime exceptions during
     * audio capture setup and streaming operation.
     * - Ensures proper resource cleanup through `try-with-resources` for audio lines and sends completion signals
     * to the stream observer.
     * <p>
     * Preconditions:
     * - Assumes `speechClient` is properly initialized and configured for speech recognition.
     * - Relies on class-level configurations for sample rate, buffer size, VAD thresholds, and streaming duration.
     */
    private void startStreaming() {
        while (isListening.get()) {
            log.info("Starting new streaming session...");
            long streamStartTime = System.currentTimeMillis();
            ApiStreamObserver<StreamingRecognizeRequest> requestObserver = null;

            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);

                StreamingRecognitionConfig streamingConfig = getStreamingRecognitionConfig();

                List<StreamingRecognizeRequest> requests = new ArrayList<>();
                requests.add(StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build());
                if (lastBuffer != null) {
                    requests.add(StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(lastBuffer)).build());
                }

                requestObserver = speechClient.streamingRecognizeCallable().bidiStreamingCall(
                        new ApiStreamObserver<>() {
                            @Override
                            public void onNext(StreamingRecognizeResponse response) {
                                for (StreamingRecognitionResult result : response.getResultsList()) {
                                    processStreamingRecognitionResult(result);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                log.error("STT error: {}", t.getMessage(), t);
                            }

                            @Override
                            public void onCompleted() {
                                log.info("STT streaming completed");
                            }
                        }
                );

                for (StreamingRecognizeRequest request : requests) {
                    requestObserver.onNext(request);
                }

                AudioFormat format = new AudioFormat(sampleRateHertz, 16, CHANNELS, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
                    log.info("Using streaming format: SampleRate={}, Channels={}", sampleRateHertz, CHANNELS);
                    line.open(format, bufferSize);
                    line.start();
                    byte[] buffer = new byte[bufferSize];

                    // Reset VAD state per session
                    isActive = false;
                    consecutiveVoice = 0;
                    consecutiveSilence = 0;

                    while (isListening.get() && line.isOpen() && (System.currentTimeMillis() - streamStartTime) < STREAM_DURATION_MS) {
                        int bytesRead = line.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            byte[] trimmedBuffer = new byte[bytesRead];
                            System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);

                            // Compute RMS for VAD
                            double rms = calculateRMS(trimmedBuffer, bytesRead);

                            // Update state with hysteresis
                            if (rms > RMS_THRESHOLD_HIGH) {
                                consecutiveVoice++;
                                consecutiveSilence = 0;
                            } else {
                                consecutiveVoice = 0;
                                if (rms < RMS_THRESHOLD_LOW) {
                                    consecutiveSilence++;
                                } else {
                                    consecutiveSilence = 0;
                                }
                            }

                            if (!isActive && consecutiveVoice >= ENTER_VOICE_FRAMES) {
                                isActive = true;
                                log.debug("VAD: Entered active state (voice detected)");
                            }
                            if (isActive && consecutiveSilence >= EXIT_SILENCE_FRAMES) {
                                isActive = false;
                                log.debug("VAD: Exited active state (silence detected)");
                            }

                            long currentTime = System.currentTimeMillis();
                            boolean sendKeepAlive = (currentTime - lastAudioSentTime) >= KEEP_ALIVE_INTERVAL_MS;

                            if (isActive || sendKeepAlive) {
                                requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                                        .setAudioContent(ByteString.copyFrom(trimmedBuffer))
                                        .build());
                                lastAudioSentTime = currentTime;
                                if (sendKeepAlive && !isActive) {
                                    log.debug("Sending keep-alive (silence)");
                                } else {
                                    log.debug("Sending voice audio, RMS: {}", rms);
                                }
                            } else {
                                log.debug("Skipping send (silence, RMS: {})", rms);
                            }

                            // Update lastBuffer for continuity on restart
                            lastBuffer = trimmedBuffer;
                        }
                    }
                } catch (LineUnavailableException | IllegalArgumentException e) {
                    log.error("Audio capture failed: {}", e.getMessage());
                } finally {
                    if (requestObserver != null) {
                        requestObserver.onCompleted();
                    }
                    try {
                        Thread.sleep(RESTART_DELAY_MS);
                    } catch (InterruptedException e) {
                        log.warn("Restart delay interrupted. System exiting?", e);
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.error("Streaming recognition failed: {}", e.getMessage());
                try {
                    Thread.sleep(RESTART_DELAY_MS);
                } catch (InterruptedException ie) {
                    log.error("Restart delay interrupted", ie);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Calculates the Root Mean Square (RMS) value of a given audio buffer.
     * The RMS value provides an estimation of the signal's amplitude.
     * It processes 16-bit little-endian samples from the provided byte array.
     *
     * @param buffer the byte array containing the audio data in little-endian format
     * @param length the number of bytes to process within the buffer
     * @return the RMS value of the audio signal represented in the buffer
     */
    private double calculateRMS(byte[] buffer, int length) {
        if (length < 2) return 0.0;
        double sum = 0.0;
        int samples = length / 2; // 16-bit samples
        for (int i = 0; i < length; i += 2) {
            // Little-endian signed 16-bit
            int val = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
            if (val > 32767) val -= 65536; // Convert to signed
            sum += (double) val * val;
        }
        return Math.sqrt(sum / samples);
    }


    /**
     * Processes the result of a streaming speech recognition operation.
     * Extracts the most probable transcription alternative, discards low-confidence or
     * irrelevant transcriptions, and queues them for further processing when applicable.
     * Sanitizes and delegates commands to an AI system based on streaming mode settings and keywords.
     *
     * @param result the {@link StreamingRecognitionResult} object containing recognition
     *               alternatives, confidence scores, and finality status for a segment of
     *               streaming audio transcription
     */
    private void processStreamingRecognitionResult(StreamingRecognitionResult result) {
        if (result.getAlternativesCount() > 0) {
            SpeechRecognitionAlternative alt = result.getAlternatives(0);
            String transcript = alt.getTranscript().trim();
            float confidence = alt.getConfidence();
            if (result.getIsFinal()) {
                EventBusManager.publish(new AppLogEvent("STT Heard: [" + transcript + "]. Confidence: " + confidence + "."));
                if (!transcript.isBlank() && transcript.length() >= 3 && confidence > 0.3) {
                    transcriptionQueue.offer(transcript);
                    log.info("Final transcript: {} (confidence: {})", transcript, confidence);
                    String sanitizedTranscript = DaftSecretarySanitizer.getInstance().correctMistakes(transcript);

                    EventBusManager.publish(new AppLogEvent("STT Sanitized: [" + sanitizedTranscript + "]."));

                    boolean isStreamingModeOn = SystemSession.getInstance().isStreamingModeOn();
                    if (isStreamingModeOn) {
                        String voiceName = SystemSession.getInstance().getAIVoice().getName();
                        if (sanitizedTranscript.toLowerCase().startsWith("computer") || sanitizedTranscript.toLowerCase().startsWith(voiceName.toLowerCase())) {
                            sendToAi(sanitizedTranscript.replace("computer,", "").replace(voiceName.toLowerCase() + ",", ""), confidence);
                        }
                    } else {
                        sendToAi(sanitizedTranscript, confidence);
                    }
                } else {
                    log.info("Discarded transcript: {} (confidence: {})", transcript, confidence);
                }
            }
        }
    }

    /**
     * Sends a sanitized transcript of user input and its associated confidence score to an AI processing system.
     * This method logs the provided sanitized transcript for debugging purposes and publishes a
     * {@link UserInputEvent} on the {@link EventBusManager} for further handling by registered listeners.
     *
     * @param sanitizedTranscript the sanitized transcription of the user's input
     * @param confidence          the confidence score of the transcription, represented as a float
     */
    private void sendToAi(String sanitizedTranscript, float confidence) {
        log.info("Processing sanitizedTranscript: {}", sanitizedTranscript);
        EventBusManager.publish(new UserInputEvent(sanitizedTranscript, confidence));
    }

    /**
     * Constructs and returns a configured {@link StreamingRecognitionConfig} object for use in
     * streaming speech recognition. This configuration is tailored for handling audio in the
     * "en-US" language code, enabling features such as automatic punctuation, word time offsets,
     * and contextual phrase boosting.
     * <p>
     * The method:
     * - Configures a {@link SpeechContext} to add contextual phrases with an associated boost value.
     * - Sets up a {@link RecognitionConfig} with audio encoding, sample rate, channel count, model type,
     * and other recognition options.
     * - Builds a {@link StreamingRecognitionConfig} enabling specific streaming features such as
     * interim result reporting for ongoing audio streams.
     *
     * @return a {@link StreamingRecognitionConfig} object configured for streaming recognition with
     * language support, phrase context boosting, interim result updates, and other
     * recognition options.
     */
    private StreamingRecognitionConfig getStreamingRecognitionConfig() {
        SpeechContext commandContext = SpeechContext.newBuilder()
                .addAllPhrases(AiRequestHints.COMMON_PHRASES)
                .setBoost(35.0f)
                .build();

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(sampleRateHertz)
                .setAudioChannelCount(CHANNELS)
                .setLanguageCode("en-US")
                .setEnableAutomaticPunctuation(true)
                .addSpeechContexts(commandContext)
                .setModel("phone_call")
                .setEnableWordTimeOffsets(true)
                .build();

        return StreamingRecognitionConfig.newBuilder()
                .setConfig(config)
                .setInterimResults(true)
                .setSingleUtterance(false)
                .build();
    }

    private void stopListening() {
        isListening.set(false);
    }

    public void shutdown() {
        stopListening();
        aiCommandInterface.stop();
        if (speechClient != null) {
            speechClient.close();
            log.info("SpeechClient closed");
        }
        //for debugging
        //stopWavRecording();
    }


    /// ////////////////////////////////////////////////////////////////////////////////
    //for debugging audio quality
    private void stopWavRecording() {
        if (audioInputStream != null) {
            try {
                audioInputStream.close();
                log.info("Stopped WAV recording");
            } catch (Exception e) {
                log.error("Failed to stop WAV recording", e);
            }
        }
    }

    //for debugging audio quality
    private void startWavRecording() {
        try {
            AudioFormat format = new AudioFormat(sampleRateHertz, 16, CHANNELS, true, false);
            wavFile = new File("audio_debug.wav");
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, bufferSize);
            line.start();
            audioInputStream = new AudioInputStream(line);
            new Thread(() -> {
                try {
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);
                    log.info("Started WAV recording to {}", wavFile.getAbsolutePath());
                } catch (Exception e) {
                    log.error("Failed to write WAV file", e);
                }
            }).start();
        } catch (Exception e) {
            log.error("Failed to start WAV recording", e);
        }
    }
}
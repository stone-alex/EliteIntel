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
import elite.companion.util.StringSanitizer;
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
 * The GoogleSTTImpl class is an implementation of the EarsInterface that integrates with Google Speech-to-Text (STT)
 * API to perform voice-to-text operations. It streams audio data to Google STT, processes recognition results, and
 * interacts with AI commands as needed.
 * <p>
 * It provides methods to manage the audio streaming process, handle transcription results, and configure Google
 * STT settings. This class is designed to handle continuous or interrupted streaming of audio, adapting to audio
 * formats and thresholds dynamically.
 * <p>
 * Fields:
 * - log: Logger utility for debugging and monitoring.
 * - sampleRateHertz: Sample rate in Hertz for audio input.
 * - bufferSize: Byte size of audio buffer used for processing.
 * - CHANNELS: Number of audio channels utilized.
 * - RESTART_DELAY_MS: Delay time in milliseconds before restarting a stream.
 * - wavFile: File handler for WAV recordings.
 * - transcriptionQueue: Queue to store transcribed text from Google STT.
 * - speechClient: Client object to communicate with Google Speech-to-Text.
 * - aiCommandInterface: Interface to handle AI commands based on transcriptions.
 * - isListening: Flag indicating if the system is currently listening for audio.
 * - lastAudioSentTime: Timestamp of the last audio data sent for streaming.
 * - lastBuffer: The last audio buffer processed.
 * - audioInputStream: Input stream for capturing microphone audio.
 * - RMS_THRESHOLD_HIGH: Root Mean Square (RMS) value threshold for determining loud voices.
 * - RMS_THRESHOLD_LOW: RMS value threshold for determining silence.
 * - ENTER_VOICE_FRAMES: Consecutive audio frames required to detect active voice.
 * - EXIT_SILENCE_FRAMES: Consecutive audio frames required to detect silence.
 * - STREAM_DURATION_MS: Duration of each audio stream in milliseconds.
 * - KEEP_ALIVE_INTERVAL_MS: Interval for keep-alive messages during streaming.
 * - isActive: Flag indicating if processing is active.
 * - consecutiveVoice: Counter for consecutive frames of active voice detected.
 * - consecutiveSilence: Counter for consecutive frames of silence detected.
 * - processingThread: Thread responsible for audio streaming and processing.
 * <p>
 * Methods:
 * - stop(): Stops the audio streaming or listening process.
 * - start(): Starts the audio streaming or listening process.
 * - GoogleSTTImpl(): Constructor to initialize all required resources and configurations.
 * - detectAudioFormat(): Detects the audio format of the input audio stream.
 * - stopWavRecording(): Stops recording audio to a WAV file.
 * - startWavRecording(): Initiates recording of audio to a WAV file.
 * - startStreaming(): Streams audio to Google STT and processes the transcription results. In case of a
 *   prolonged session, the method restarts the stream periodically.
 * - calculateRMS(byte[] buffer, int length): Calculates the RMS value of an audio buffer to determine sound
 *   thresholds dynamically.
 * - processStreamingRecognitionResult(StreamingRecognitionResult result): Handles the processing of the
 *   streaming recognition result from Google STT and focuses on the final recognized result.
 * - sendToAi(String sanitizedTranscript, float confidence): Sends processed transcription to an AI
 *   command interface for further actions.
 * - getStreamingRecognitionConfig(): Creates a streaming recognition configuration tailored to the
 *   application's needs, including speech adaptations, domain-specific terms, and context.
 * - getNextTranscription(): Retrieves the next available transcription from the queue. This method may
 *   block if no transcriptions are currently available.
 * - stopListening(): Stops the listening process without shutting down other operations.
 * - shutdown(): Completely shuts down all operations, closes resources, and releases any held resources.
 * <p>
 * Superclasses:
 * - java.lang.Object: Base class in the Java hierarchy.
 * - elite.companion.comms.ears.EarsInterface: Interface defining the general structure for listening and
 *   processing ears implementations.
 */
public class GoogleSTTImpl implements EarsInterface {
    private static final Logger log = LoggerFactory.getLogger(GoogleSTTImpl.class);
    private int sampleRateHertz; // Dynamically detected
    private int bufferSize; // Dynamically calculated based on sample rate
    private static final int CHANNELS = 1; // Mono
    //private static final int KEEP_ALIVE_INTERVAL_MS = 2000;
    //private static final int STREAM_DURATION_MS = 30000; // 30s
    private static final int RESTART_DELAY_MS = 50; // 50ms sleep after stream close
    private File wavFile = null; // Output WAV for debugging
    private final BlockingQueue<String> transcriptionQueue = new LinkedBlockingQueue<>();
    private SpeechClient speechClient;
    private final AiCommandInterface aiCommandInterface;
    private final AtomicBoolean isListening = new AtomicBoolean(true);
    private long lastAudioSentTime = System.currentTimeMillis();
    private byte[] lastBuffer = null;
    private AudioInputStream audioInputStream = null;

    private static final double RMS_THRESHOLD_HIGH = 800.0; // Configurable: Enter active on voice (calibrate: 500-1500 typical)
    private static final double RMS_THRESHOLD_LOW = 300.0; // Configurable: Exit active on sustained silence
    private static final int ENTER_VOICE_FRAMES = 1; // Quick enter to avoid initial clipping (100ms buffer = ~0.1s delay max)
    private static final int EXIT_SILENCE_FRAMES = 20; // ~2s silence to exit (handles pauses in speech)
    private static final int STREAM_DURATION_MS = 300000; // 5 min; matches Google V1 limit
    private static final int KEEP_ALIVE_INTERVAL_MS = 8000; // Increase to 5s to minimize silence sends (5000 is a good number)

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
     * Starts the Google speech-to-text (STT) service by initializing necessary configurations
     * and launching the audio processing in a separate thread. This method ensures proper
     * setup of the speech client, manages threading, and delegates necessary audio and AI-related
     * operations.
     * <p>
     * Key Steps:
     * - Checks if the speech recognition service is already running. If active, logs a warning and exits.
     * - Detects and configures the audio format including sample rate and buffer size by calling the
     * detectAudioFormat method.
     * - Retrieves the system-wide STT API key from the configuration. If the key is invalid or not found,
     * logs an error and terminates the startup process.
     * - Initializes a SpeechClient instance using the retrieved API key to facilitate Google STT operations.
     * - Starts a new thread to handle audio streaming to Google STT via the startStreaming method.
     * - Initializes any dependent AI interface for processing recognized speech commands.
     * <p>
     * Logs:
     * - Warns if the service is already running.
     * - Logs success or failure during SpeechClient initialization.
     * - Logs errors related to missing API keys or runtime exceptions during startup.
     * <p>
     * Exceptions:
     * - Throws RuntimeException if initialization of the SpeechClient or the AI command interface fails.
     */
    @Override public void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("Speech recognition is already running");
            return;
        }

        detectAudioFormat(); // Detect sample rate and set buffer size

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
        //this.aiCommandInterface = new GrokCommandEndPoint();
        this.aiCommandInterface = ApiFactory.getInstance().getCommandEndpoint();
    }

    /**
     * Detects a supported audio format for mono 16-bit input by checking available sample rates
     * and sets the sample rate and buffer size accordingly. The method iterates over a predefined
     * list of preferred sample rates (48000 Hz, 44100 Hz, 16000 Hz) to determine the first one
     * that is supported by the underlying audio system.
     *
     * If a supported sample rate is found, it sets up the audio configuration using the detected
     * sample rate and calculates the buffer size based on a ~100 ms duration.
     *
     * Logging provides details of the selected sample rate and corresponding buffer size for
     * clarity during runtime.
     *
     * Throws:
     * - RuntimeException if no supported audio format is found, indicating that the system does not
     *   support the required configuration for mono 16-bit input.
     */
    private void detectAudioFormat() {
        int[] possibleRates = {48000, 44100, 16000}; // Preferred rates in order
        for (int rate : possibleRates) {
            AudioFormat format = new AudioFormat(rate, 16, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (AudioSystem.isLineSupported(info)) {
                this.sampleRateHertz = rate;
                this.bufferSize = (int) (rate * 0.1 * 2 * CHANNELS); // ~100ms buffer
                log.info("Detected supported sample rate: {} Hz, buffer size: {}", sampleRateHertz, bufferSize);
                return;
            }
        }
        throw new RuntimeException("No supported audio format found for mono 16-bit input");
    }


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

    /**
     * Initiates a streaming audio session for Google Speech-to-Text (STT) recognition.
     * This method repeatedly attempts to capture audio from the system microphone, process it in real-time,
     * and send it to the Google Speech API for transcription. It manages voice activity detection (VAD),
     * keep-alive packets, and session reinitialization upon completion or failure.
     *
     * Key Functions:
     * - Configures the audio input format and initializes audio capture through `TargetDataLine`.
     * - Creates and manages an `ApiStreamObserver` for bidirectional streaming communication with the Google Speech API.
     * - Sends initial configuration and buffered audio data if available at the start of streaming.
     * - Reads audio data from the microphone, processes it, and performs voice activity detection to determine whether
     *   to send audio blocks or silence-based keep-alive packets.
     * - Manages streaming session duration and reinitializes upon session end or error.
     *
     * Logs:
     * - Logs audio session states, including initialization, audio format details, and VAD transitions.
     * - Provides debug logs for active audio packets and keep-alive transmissions.
     * - Captures and logs errors, including audio capture issues and streaming session failures.
     *
     * Threading:
     * Operates within a loop that respects the `isListening` flag, ensuring the session halts cleanly
     * when directed to stop. Uses thread priorities for performance optimization. Implements delays
     * between session restarts to avoid overloading resources.
     *
     * Exceptions:
     * - Handles `LineUnavailableException`, `IllegalArgumentException`, and other runtime exceptions during
     *   audio capture setup and streaming operation.
     * - Ensures proper resource cleanup through `try-with-resources` for audio lines and sends completion signals
     *   to the stream observer.
     *
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
                                log.error("STT error: {}", t.getMessage());
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
                                //log.debug("Skipping send (silence, RMS: {})", rms);
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
                        log.error("Restart delay interrupted", e);
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
     * Sanitizes and delegates commands to an AI system based on privacy settings and keywords.
     *
     * @param result the {@link StreamingRecognitionResult} object containing recognition
     *               alternatives, confidence scores, and finality status for a segment of
     *               streaming audio transcription
     */
    private void processStreamingRecognitionResult(StreamingRecognitionResult result) {
        if (result.getAlternativesCount() > 0) {
            SpeechRecognitionAlternative alt = result.getAlternatives(0);
            String transcript = alt.getTranscript();
            float confidence = alt.getConfidence();
            if (result.getIsFinal()) {
                EventBusManager.publish(new AppLogEvent("STT Heard: [" + transcript + "]. Confidence: " + confidence + "."));
                if (!transcript.isBlank() && transcript.length() >= 3 && confidence > 0.3) {
                    transcriptionQueue.offer(transcript);
                    log.info("Final transcript: {} (confidence: {})", transcript, confidence);
                    String sanitizedTranscript = StringSanitizer.sanitizeGoogleMistakes(transcript);

                    EventBusManager.publish(new AppLogEvent("STT Sanitized: [" + sanitizedTranscript + "]."));

                    boolean isPrivacyModeOn = SystemSession.getInstance().isStreamingModeOn();
                    if (isPrivacyModeOn) {
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
     * @param confidence the confidence score of the transcription, represented as a float
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
     *
     * The method:
     * - Configures a {@link SpeechContext} to add contextual phrases with an associated boost value.
     * - Sets up a {@link RecognitionConfig} with audio encoding, sample rate, channel count, model type,
     *   and other recognition options.
     * - Builds a {@link StreamingRecognitionConfig} enabling specific streaming features such as
     *   interim result reporting for ongoing audio streams.
     *
     * @return a {@link StreamingRecognitionConfig} object configured for streaming recognition with
     *         language support, phrase context boosting, interim result updates, and other
     *         recognition options.
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

    @Override public String getNextTranscription() throws InterruptedException {
        return transcriptionQueue.take();
    }

    @Override public void stopListening() {
        isListening.set(false);
    }

    @Override public void shutdown() {
        stopListening();
        aiCommandInterface.stop();
        if (speechClient != null) {
            speechClient.close();
            log.info("SpeechClient closed");
        }
        //for debugging
        //stopWavRecording();
    }
}
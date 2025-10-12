package elite.intel.ai.ears.google;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import elite.intel.ai.ConfigManager;
import elite.intel.ai.ears.*;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.DaftSecretarySanitizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GoogleSTTImpl implements EarsInterface {
    private static final Logger log = LogManager.getLogger(GoogleSTTImpl.class);

    private int sampleRateHertz;  // Dynamically detected
    private int bufferSize; // Dynamically calculated based on sample rate
    private double RMS_THRESHOLD_HIGH; // Dynamically calibrated
    private double RMS_THRESHOLD_LOW; // Dynamically calibrated

    private static final int CHANNELS = 1; // Mono
    private static final int STREAM_DURATION_MS = 290000; // ~4 min 50 sec; below Google V1 limit for safety
    private static final int KEEP_ALIVE_INTERVAL_MS = 3000; // Keep-alive interval
    private static final int ENTER_VOICE_FRAMES = 1; // Quick enter to avoid clipping
    private static final int EXIT_SILENCE_FRAMES = 10; // ~1s silence to exit

    private File wavFile = null; // Output WAV for debugging
    private SpeechClient speechClient;
    private final AtomicBoolean isListening = new AtomicBoolean(true);
    private long lastAudioSentTime = System.currentTimeMillis();
    private byte[] lastBuffer = null;
    private AudioInputStream audioInputStream = null;

    private boolean isActive = false;
    private int consecutiveVoice = 0;
    private int consecutiveSilence = 0;
    private Thread processingThread;
    Map<String, String> corrections;

    public GoogleSTTImpl() {
    }

    @Override
    public void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("Speech recognition is already running");
            return;
        }
        corrections = DaftSecretarySanitizer.getInstance().getCorrections();
        // Reset isListening to true before starting a new thread
        isListening.set(true);

        // Detect audio format
        AudioSettingsTuple<Integer, Integer> formatResult = AudioFormatDetector.detectSupportedFormat();
        this.sampleRateHertz = formatResult.getSampleRate();
        this.bufferSize = formatResult.getBufferSize();

        SystemSession systemSession = SystemSession.getInstance();
        Double rms_threshold_high = systemSession.getRmsThresholdHigh();
        Double rms_threshold_low = systemSession.getRmsThresholdLow();

        if (rms_threshold_high == null || rms_threshold_low == null) {
            // Calibrate RMS thresholds
            EventBusManager.publish(new AppLogEvent("Calibrating audio..."));
            AudioSettingsTuple<Double, Double> rmsThresholds = AudioCalibrator.calibrateRMS(sampleRateHertz, bufferSize);
            this.RMS_THRESHOLD_HIGH = rmsThresholds.getSampleRate(); // First tuple element
            this.RMS_THRESHOLD_LOW = rmsThresholds.getBufferSize();  // Second tuple element
        } else {
            this.RMS_THRESHOLD_HIGH = rms_threshold_high;
            this.RMS_THRESHOLD_LOW = rms_threshold_low;
            log.info("RMS thresholds already calibrated: High: {}, Low: {}", this.RMS_THRESHOLD_HIGH, this.RMS_THRESHOLD_LOW);
        }

        // Initialize SpeechClient
        try {
            this.speechClient = createSpeechClient();
            log.info("SpeechClient initialized successfully with API key");
        } catch (Exception e) {
            log.error("Failed to initialize SpeechClient: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize SpeechClient", e);
        }

        // Start processing thread
        this.processingThread = new Thread(this::startStreaming);
        this.processingThread.start();
        if (SystemSession.getInstance().getRmsThresholdLow() != null) {
            EventBusManager.publish(new AiVoxResponseEvent("Voice Input Enabled"));
        }
    }

    @Override
    public void stop() {
        isListening.set(false);
        if (speechClient != null) {
            speechClient.close();
            log.info("SpeechClient closed");
        }
        if (processingThread != null) {
            this.processingThread.interrupt();
        }
        EventBusManager.publish(new AiVoxResponseEvent("Voice input disabled."));
    }

    @SuppressWarnings("deprecation") private void startStreaming() { // v2 is not an option as it is for SAAS v1 uses bidiStreamingCall and there is no upgrade
        int retryCount = 0;
        StringBuffer currentTranscript = new StringBuffer();
        List<Float> confidences = Collections.synchronizedList(new ArrayList<>());
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
                                    processStreamingRecognitionResult(result, currentTranscript, confidences);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                log.error("STT error: {}", t.getMessage());
                                // Event will attempt service re-start in 20 seconds
                                stop();
                                EventBusManager.publish(new STTConnectionFailed());
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
                            boolean wasActive = isActive;
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

                            // Check for VAD exit and send accumulated transcript if any
                            if (wasActive && !isActive && currentTranscript.length() > 0) {
                                String fullTranscript = currentTranscript.toString().trim();
                                EventBusManager.publish(new AppLogEvent("STT Heard: [" + fullTranscript + "]."));
                                if (!fullTranscript.isBlank() && fullTranscript.length() >= 3) {
                                    log.info("Final accumulated transcript: {}", fullTranscript);
                                    String sanitizedTranscript = DaftSecretarySanitizer.getInstance().correctMistakes(fullTranscript);

                                    EventBusManager.publish(new AppLogEvent("STT Sanitized: [" + sanitizedTranscript + "]."));
                                    boolean isStreamingModeOn = SystemSession.getInstance().isStreamingModeOn();
                                    float avgConfidence;
                                    synchronized (confidences) {
                                        avgConfidence = (float) confidences.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
                                    }
                                    if (avgConfidence > 0.3) {
                                        if (isStreamingModeOn) {
                                            String voiceName = SystemSession.getInstance().getAIVoice().getName();
                                            if (sanitizedTranscript.toLowerCase().startsWith("computer") || sanitizedTranscript.toLowerCase().contains(voiceName.toLowerCase())) {
                                                sendToAi(sanitizedTranscript.replace("computer,", "").replace(voiceName.toLowerCase() + ",", ""), 100);
                                            }
                                        } else {
                                            sendToAi(sanitizedTranscript, avgConfidence);
                                        }
                                    } else {
                                        log.info("Discarded transcript: {} (avg confidence: {})", fullTranscript, avgConfidence);
                                    }
                                }
                                currentTranscript.setLength(0);
                                synchronized (confidences) {
                                    confidences.clear();
                                }
                            }

                            // Update lastBuffer for continuity on restart
                            lastBuffer = trimmedBuffer;
                        }
                    }
                } catch (LineUnavailableException | IllegalArgumentException e) {
                    log.error("Audio capture failed: {}", e.getMessage());
                } finally {
                    requestObserver.onCompleted();
                    // Send any pending transcript on stream close
                    if (!isActive && currentTranscript.length() > 0) {
                        String fullTranscript = currentTranscript.toString().trim();
                        EventBusManager.publish(new AppLogEvent("STT Heard: [" + fullTranscript + "]."));
                        if (!fullTranscript.isBlank() && fullTranscript.length() >= 3) {
                            log.info("Final accumulated transcript: {}", fullTranscript);
                            String sanitizedTranscript = DaftSecretarySanitizer.getInstance().correctMistakes(fullTranscript);

                            EventBusManager.publish(new AppLogEvent("STT Sanitized: [" + sanitizedTranscript + "]."));
                            boolean isStreamingModeOn = SystemSession.getInstance().isStreamingModeOn();
                            float avgConfidence;
                            synchronized (confidences) {
                                avgConfidence = (float) confidences.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
                            }
                            if (avgConfidence > 0.3) {
                                if (isStreamingModeOn) {
                                    String voiceName = SystemSession.getInstance().getAIVoice().getName();
                                    if (sanitizedTranscript.toLowerCase().startsWith("computer") || sanitizedTranscript.toLowerCase().startsWith(voiceName.toLowerCase())) {
                                        sendToAi(sanitizedTranscript.replace("computer,", "").replace(voiceName.toLowerCase() + ",", ""), avgConfidence);
                                    }
                                } else {
                                    sendToAi(sanitizedTranscript, avgConfidence);
                                }
                            } else {
                                log.info("Discarded transcript: {} (avg confidence: {})", fullTranscript, avgConfidence);
                            }
                        }
                        currentTranscript.setLength(0);
                        synchronized (confidences) {
                            confidences.clear();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Streaming recognition failed: {}", e.getMessage());
                EventBusManager.publish(new AppLogEvent("STT max retries hit; pausing listener."));
                stop();
            }
        }
    }

    private SpeechClient createSpeechClient() throws Exception {
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.STT_API_KEY);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("STT API key not found in system.conf");
            throw new IllegalStateException("STT API key missing");
        }
        SpeechSettings settings = SpeechSettings.newBuilder()
                .setApiKey(apiKey)
                .build();
        return SpeechClient.create(settings);
    }

    private double calculateRMS(byte[] buffer, int length) {
        if (length < 2) return 0.0;
        double sum = 0.0;
        int samples = length / 2; // 16-bit samples
        for (int i = 0; i < length; i += 2) {
            int val = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
            if (val > 32767) val -= 65536; // Convert to signed
            sum += (double) val * val;
        }
        return Math.sqrt(sum / samples);
    }

    private void processStreamingRecognitionResult(StreamingRecognitionResult result, StringBuffer currentTranscript, List<Float> confidences) {
        if (result.getAlternativesCount() > 0) {
            SpeechRecognitionAlternative alt = result.getAlternatives(0);
            String transcript = alt.getTranscript().trim();
            float confidence = alt.getConfidence();
            if (result.getIsFinal()) {
                currentTranscript.append(transcript).append(" ");
                synchronized (confidences) {
                    confidences.add(confidence);
                }
            }
        }
    }

    private void sendToAi(String sanitizedTranscript, float confidence) {
        boolean hasAiReference = !sanitizedTranscript.isBlank()
                && (sanitizedTranscript.toLowerCase().contains("computer"))
                || (sanitizedTranscript.toLowerCase().contains(SystemSession.getInstance().getAIVoice().getName().toLowerCase()));

        EventBusManager.publish(new TTSInterruptEvent(hasAiReference));
        AudioPlayer.getInstance().playBeep(); //user notification, we are processing the input now.
        log.info("Processing sanitizedTranscript: {}", sanitizedTranscript);
        EventBusManager.publish(new UserInputEvent(sanitizedTranscript, confidence));
    }

    private StreamingRecognitionConfig getStreamingRecognitionConfig() {

        Set<String> correctionSet = new HashSet<>(corrections.values());
        SpeechContext commandContext = SpeechContext.newBuilder()
                .addAllPhrases(correctionSet)
                .setBoost(20.0f)
                .build();

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(sampleRateHertz)
                .setAudioChannelCount(CHANNELS)
                .setLanguageCode("en-US")
                .setEnableAutomaticPunctuation(false)
                .addSpeechContexts(commandContext)
                .setModel("latest_long")
                .setEnableWordTimeOffsets(true)
                .build();

        return StreamingRecognitionConfig.newBuilder()
                .setConfig(config)
                .setInterimResults(true)
                .setSingleUtterance(false)
                .build();
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
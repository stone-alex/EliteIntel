package elite.intel.ai.ears.google;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.speech.v1.*;
import com.google.common.eventbus.Subscribe;
import com.google.protobuf.ByteString;
import elite.intel.ai.ears.*;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.STTSanitizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GoogleSTTImpl implements EarsInterface {
    public static final double MIN_CONFIDENCE_LEVEL = 0.3; // 1 = 100%
    private static final int TARGET_SAMPLE_RATE = 48000;  // Google loves 48k best quality/cost
    private static final Logger log = LogManager.getLogger(GoogleSTTImpl.class);
    private static final int CHANNELS = 1; // Mono
    private static final int STREAM_DURATION_MS = 290000; // ~4 min 50 sec; below Google V1 limit
    private static final int KEEP_ALIVE_INTERVAL_MS = 250; // Reduced for timeout fix
    private static final int KEEP_ALIVE_BUFFER_SIZE = 320; // ~10ms at 16000Hz; minimal for cost
    private static final int ENTER_VOICE_FRAMES = 1; // Quick enter to avoid clipping
    private static final int EXIT_SILENCE_FRAMES = 15; // ~1s silence to exit
    private static final long BASE_BACKOFF_MS = 2000; // Base backoff for retries
    private static final long MAX_BACKOFF_MS = 60000; // Cap at 1 min

    private static final long MIN_STREAM_GAP_MS = 100;

    private final AtomicBoolean isListening = new AtomicBoolean(true);
    private final AtomicBoolean isSpeaking = new AtomicBoolean(false);
    private final SystemSession systemSession = SystemSession.getInstance();
    Map<String, String> corrections;
    private Resampler resampler;  // lazy-init when needed
    private int sampleRateHertz;  // Dynamically detected
    private int bufferSize; // Dynamically calculated based on sample rate
    private double RMS_THRESHOLD_HIGH; // Dynamically calibrated
    private double RMS_THRESHOLD_LOW; // Dynamically calibrated
    private SpeechClient speechClient;
    private long lastAudioSentTime = System.currentTimeMillis();
    private Thread processingThread;

    public GoogleSTTImpl() {
        EventBusManager.register(this);
    }

    @Override
    public void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("Speech recognition is already running");
            return;
        }
        corrections = STTSanitizer.getInstance().getCorrections();
        isListening.set(true);

        // Detect audio format
        AudioSettingsTuple<Integer, Integer> formatResult = AudioFormatDetector.detectSupportedFormat();
        this.sampleRateHertz = formatResult.getSampleRate();
        this.bufferSize = formatResult.getBufferSize();

        Double rms_threshold_high = systemSession.getRmsThresholdHigh();
        Double rms_threshold_low = systemSession.getRmsThresholdLow();

        if (rms_threshold_high == 0 || rms_threshold_low == 0) {
            EventBusManager.publish(new AppLogEvent("Calibrating audio..."));
            RmsTupple<Double, Double> rmsThresholds = AudioCalibrator.calibrateRMS(sampleRateHertz, bufferSize);
            this.RMS_THRESHOLD_HIGH = rmsThresholds.getRmsHigh();
            this.RMS_THRESHOLD_LOW = rmsThresholds.getRmsLow();
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
        if (systemSession.getRmsThresholdLow() != null) {
            EventBusManager.publish(new AppLogEvent("Voice Input Enabled"));
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

    @SuppressWarnings("deprecation") private void startStreaming() {

        if (sampleRateHertz != TARGET_SAMPLE_RATE) {
            resampler = new Resampler(sampleRateHertz, TARGET_SAMPLE_RATE, CHANNELS);
            log.info("Enabled live downsampling {} → {} Hz", sampleRateHertz, TARGET_SAMPLE_RATE);
        }

        int retryCount = 0;
        StringBuffer currentTranscript = new StringBuffer();
        List<Float> confidences = Collections.synchronizedList(new ArrayList<>());
        long lastStreamStart = 0; // Track stream start frequency
        AtomicBoolean needRestart = new AtomicBoolean(false);

        while (isListening.get()) {
            // Enforce minimum gap between stream starts
            long timeSinceLastStream = System.currentTimeMillis() - lastStreamStart;
            if (timeSinceLastStream < MIN_STREAM_GAP_MS) {
                long delay = MIN_STREAM_GAP_MS - timeSinceLastStream;
                log.info("Enforcing stream gap, waiting {}ms", delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    log.warn("Stream gap wait interrupted");
                    break;
                }
            }

            log.info("Starting new streaming session... (Time since last start: {}ms)", System.currentTimeMillis() - lastStreamStart);
            lastStreamStart = System.currentTimeMillis();
            ApiStreamObserver<StreamingRecognizeRequest> requestObserver = null;


            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);

                StreamingRecognitionConfig streamingConfig = getStreamingRecognitionConfig();

                List<StreamingRecognizeRequest> requests = new ArrayList<>();
                requests.add(StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build());

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
                                log.warn("STT: {}", t.getMessage());
                                needRestart.set(true);
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
                    byte[] silentBuffer = new byte[bufferSize]; // Zero-filled buffer for keep-alive

                    // Reset VAD state per session
                    boolean isActive = false;
                    int consecutiveVoice = 0;
                    int consecutiveSilence = 0;

                    while (isListening.get() && !needRestart.get() && line.isOpen() && (System.currentTimeMillis() - lastStreamStart) < STREAM_DURATION_MS) {
                        int bytesRead = line.read(buffer, 0, buffer.length);

                        byte[] audioToProcess;   // this is what we will RMS-check and send
                        int bytesToProcess;

                        if (resampler != null && bytesRead > 0) {
                            // 192→48 kHz (or any other) downsampling happens here
                            audioToProcess = resampler.resample(buffer, bytesRead);
                            bytesToProcess = audioToProcess.length;
                        } else {
                            // no resampling needed (or silence)
                            audioToProcess = (bytesRead > 0) ? buffer : silentBuffer;
                            bytesToProcess = (bytesRead > 0) ? bytesRead : silentBuffer.length;
                        }

                        // Compute RMS for VAD (use actual buffer if available)
                        double rms = bytesRead > 0 ? calculateRMS(audioToProcess, bytesToProcess) : 0.0;

                        // Update state with hysteresis
                        if (rms > RMS_THRESHOLD_HIGH) {
                            consecutiveVoice++;
                            consecutiveSilence = 0;
                            // 4 debugging
                            //EventBusManager.publish(new AppLogEvent("RMS: " + rms+" HIGH: "+RMS_THRESHOLD_HIGH+""));
                        } else {
                            consecutiveVoice = 0;
                            if (rms < RMS_THRESHOLD_HIGH) { // Let's try using RMS High for gate open and close, ignore the low
                                consecutiveSilence++;
                            } else {
                                consecutiveSilence = 0;
                            }
                        }



                        if (!isActive && consecutiveVoice >= ENTER_VOICE_FRAMES && !isSpeaking.get()) {
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
                            byte[] keepAliveBuffer = new byte[KEEP_ALIVE_BUFFER_SIZE]; // Small silent for cost savings
                            ByteString audioContent = isActive
                                    ? ByteString.copyFrom(audioToProcess, 0, bytesToProcess)
                                    : ByteString.copyFrom(keepAliveBuffer);

                            requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                                    .setAudioContent(audioContent)
                                    .build());
                            lastAudioSentTime = currentTime;
                        } else {
                            log.debug("Skipping send (silence, RMS: {})", rms);
                        }

                        // Check for VAD exit and send accumulated transcript if any
                        if (wasActive && !isActive && currentTranscript.length() > 0) {
                            String fullTranscript = currentTranscript.toString().trim();
                            EventBusManager.publish(new AppLogEvent("STT Heard: [" + fullTranscript + "]."));
                            if (!fullTranscript.isBlank() && fullTranscript.length() >= 3) {
                                log.info("Final accumulated transcript: {}", fullTranscript);
                                String sanitizedTranscript = STTSanitizer.getInstance().correctMistakes(fullTranscript);

                                EventBusManager.publish(new AppLogEvent("STT Sanitized: [" + sanitizedTranscript + "]."));
                                boolean isStreamingModeOn = systemSession.isStreamingModeOn();
                                float avgConfidence;
                                synchronized (confidences) { // Sync on the list instance for thread-safety
                                    avgConfidence = (float) confidences.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
                                }
                                if (avgConfidence > MIN_CONFIDENCE_LEVEL) {
                                    if (isStreamingModeOn) {
                                        String voiceName = systemSession.getAIVoice().getName();
                                        if (sanitizedTranscript.toLowerCase().contains("computer") || sanitizedTranscript.toLowerCase().contains(voiceName.toLowerCase())) {
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
                }
            } catch (LineUnavailableException | IllegalArgumentException e) {
                log.error("Audio capture failed: {}", e.getMessage(), e);
                needRestart.set(true);
            } catch (Exception e) {
                log.error("Streaming recognition failed: {}", e.getMessage(), e);
                needRestart.set(true);
            } finally {
                if (requestObserver != null) {
                    requestObserver.onCompleted();
                }
                // Send any pending transcript on stream close
                if (currentTranscript.length() > 0) {
                    String fullTranscript = currentTranscript.toString().trim();
                    EventBusManager.publish(new AppLogEvent("STT Heard: [" + fullTranscript + "]."));
                    if (!fullTranscript.isBlank() && fullTranscript.length() >= 3) {
                        log.info("Final accumulated transcript: {}", fullTranscript);
                        String sanitizedTranscript = STTSanitizer.getInstance().correctMistakes(fullTranscript);

                        EventBusManager.publish(new AppLogEvent("STT Sanitized: [" + sanitizedTranscript + "]."));
                        boolean isStreamingModeOn = systemSession.isStreamingModeOn();
                        float avgConfidence;
                        synchronized (confidences) {
                            avgConfidence = (float) confidences.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
                        }
                        if (avgConfidence > MIN_CONFIDENCE_LEVEL) {
                            if (isStreamingModeOn) {
                                String voiceName = systemSession.getAIVoice().getName();
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

            if (needRestart.get()) {
                long backoff = Math.min(BASE_BACKOFF_MS * (long) Math.pow(2, retryCount), MAX_BACKOFF_MS);
                log.info("Stream failed, retrying after {}ms (attempt {})", backoff, retryCount + 1);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    log.warn("Retry interrupted");
                    break;
                }
                retryCount++;
            } else {
                retryCount = 0; // Reset on success
            }
            needRestart.set(false); // Reset flag
        }
    }

    private SpeechClient createSpeechClient() throws Exception {
        String apiKey = systemSession.getSttApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("STT APIkey is not provided");
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
        EventBusManager.publish(new TTSInterruptEvent());
        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1);
        log.info("Processing sanitizedTranscript: {}", sanitizedTranscript);
        EventBusManager.publish(new UserInputEvent(sanitizedTranscript, confidence));
    }


    @Subscribe public void onIsSpeakingEvent(IsSpeakingEvent event) {
        isSpeaking.set(event.isSpeaking());
    }


    private StreamingRecognitionConfig getStreamingRecognitionConfig() {
        Set<String> correctionSet = new HashSet<>(corrections.values());
        SpeechContext commandContext = SpeechContext.newBuilder()
                .addAllPhrases(correctionSet)
                .setBoost(Float.MAX_VALUE)
                .build();

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(TARGET_SAMPLE_RATE)
                .setAudioChannelCount(CHANNELS)
                .setLanguageCode("en-US")
                .addAlternativeLanguageCodes("en-AU")
                .addAlternativeLanguageCodes("en-GB")
                .addAlternativeLanguageCodes("en-IN")
                .addAlternativeLanguageCodes("en-PH")
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
}
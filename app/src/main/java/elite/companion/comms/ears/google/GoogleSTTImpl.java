package elite.companion.comms.ears.google;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import elite.companion.comms.brain.AiCommandInterface;
import elite.companion.comms.brain.grok.GrokRequestHints;
import elite.companion.comms.ears.EarsInterface;
import elite.companion.gameapi.UserInputEvent;
import elite.companion.session.SystemSession;
import elite.companion.ui.event.AppLogEvent;
import elite.companion.util.ApiFactory;
import elite.companion.util.ConfigManager;
import elite.companion.util.EventBusManager;
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
 * NOTE $$$ This method is not free. Calls will incur charges on both Google and Grok platforms. $$$
 * <p>
 * This class connects to the Google Speech API endpoint and streams audio to it.
 * The STT (Speech-to-Text) API will return a final result which will be processed by Grok API.
 *
 */
public class GoogleSTTImpl implements EarsInterface {
    private static final Logger log = LoggerFactory.getLogger(GoogleSTTImpl.class);
    private int sampleRateHertz; // Dynamically detected
    private int bufferSize; // Dynamically calculated based on sample rate
    private static final int CHANNELS = 1; // Mono
    private static final int KEEP_ALIVE_INTERVAL_MS = 2000;
    private static final int STREAM_DURATION_MS = 30000; // 30s
    private static final int RESTART_DELAY_MS = 50; // 50ms sleep after stream close
    private File wavFile = null; // Output WAV for debugging
    private final BlockingQueue<String> transcriptionQueue = new LinkedBlockingQueue<>();
    private SpeechClient speechClient;
    private final AiCommandInterface aiCommandInterface;
    private final AtomicBoolean isListening = new AtomicBoolean(true);
    private long lastAudioSentTime = System.currentTimeMillis();
    private byte[] lastBuffer = null;
    private AudioInputStream audioInputStream = null;

    private Thread processingThread;

    @Override public void stop() {
        shutdown();
        this.processingThread.interrupt();
    }

    @Override public void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("Speech recognition is already running");
            return;
        }

        detectAudioFormat(); // Detect sample rate and set buffer size

        try {
            String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.STT_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("Google API key not found in system.conf");
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

    // Start/stop WAV recording
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
     * Stream audio to Google STT and process the final result returned as text.
     * This method will call Grok API with the final result returned from Google STT.
     * The stream runs for 30 seconds and then restarts.
     *
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
                                //log.error("STT error: {}", t.getMessage());
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

                    while (isListening.get() && line.isOpen() && (System.currentTimeMillis() - streamStartTime) < STREAM_DURATION_MS) {
                        int bytesRead = line.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            byte[] trimmedBuffer = new byte[bytesRead];
                            System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
                            long currentTime = System.currentTimeMillis();
                            if ((currentTime - lastAudioSentTime) >= KEEP_ALIVE_INTERVAL_MS) {
                                log.debug("Sending keep-alive audio, bytes read: {}", bytesRead);
                            }
                            requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                                    .setAudioContent(ByteString.copyFrom(trimmedBuffer))
                                    .build());
                            lastAudioSentTime = currentTime;
                        }
                    }
                } catch (LineUnavailableException | IllegalArgumentException e) {
                    log.error("Audio capture failed: {}", e.getMessage());
                } finally {
                    if (requestObserver != null) {
                        requestObserver.onCompleted();
                    }
                    try {
                        Thread.sleep(RESTART_DELAY_MS); // 50ms delay before restarting
                    } catch (InterruptedException e) {
                        log.error("Restart delay interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.error("Streaming recognition failed: {}", e.getMessage());
                try {
                    Thread.sleep(RESTART_DELAY_MS); // 50ms delay on failure
                } catch (InterruptedException ie) {
                    log.error("Restart delay interrupted", ie);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * We receive a stream of results from Google STT and process the final one only.
     */
    private void processStreamingRecognitionResult(StreamingRecognitionResult result) {
        if (result.getAlternativesCount() > 0) {
            SpeechRecognitionAlternative alt = result.getAlternatives(0);
            String transcript = alt.getTranscript();
            float confidence = alt.getConfidence();
            if (result.getIsFinal()) {
                if (!transcript.isBlank() && transcript.length() >= 3 && confidence > 0.3) {
                    transcriptionQueue.offer(transcript);
                    log.info("Final transcript: {} (confidence: {})", transcript, confidence);
                    String sanitizedTranscript = StringSanitizer.sanitizeGoogleMistakes(transcript);

                    EventBusManager.publish(new AppLogEvent("STT heard: " + transcript + ". Sanitized: " + sanitizedTranscript + "."));

                    boolean isPrivacyModeOn = SystemSession.getInstance().isPrivacyModeOn();
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

    private void sendToAi(String sanitizedTranscript, float confidence) {
        log.info("Processing sanitizedTranscript: {}", sanitizedTranscript);
        EventBusManager.publish(new UserInputEvent(sanitizedTranscript, confidence));
    }

    /**
     * Creates a StreamingRecognitionConfig for the Google Speech API.
     * Contains speech adaptations, domain terms and command context.
     * Use GrokRequestHints to add more as needed.
     * Sets a recognition model to phone_call. and language to en-US.
     *
     */
    private StreamingRecognitionConfig getStreamingRecognitionConfig() {
        SpeechContext commandContext = SpeechContext.newBuilder()
                .addAllPhrases(GrokRequestHints.COMMON_PHRASES)
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
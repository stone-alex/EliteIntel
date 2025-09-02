package elite.companion.comms.voice;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import elite.companion.comms.ai.GrokCommandEndPoint;
import elite.companion.comms.ai.GrokRequestHints;
import elite.companion.gameapi.UserInputEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;
import elite.companion.util.GoogleApiKeyProvider;
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
 * <p>
 * Do not call this method if you do not need to process voice commands.
 * Do not use in unit tests.
 * Requires Google json credentials to be set in resources/google_credentials.json.
 * Requires Grok API credentials to be set in resources/grok_credentials.json.
 * Do not post credentials to GitHub.
 * Do not hardocde credentials in source code.
 *
 */
public class SpeechRecognizer {
    private static final Logger log = LoggerFactory.getLogger(SpeechRecognizer.class);
    private static final int SAMPLE_RATE_HERTZ = 48000; // Locked to 48kHz
    private static final int BUFFER_SIZE = 9600; // ~100ms at 48kHz, mono
    private static final int CHANNELS = 1; // Mono
    private static final int KEEP_ALIVE_INTERVAL_MS = 2000;
    private static final int STREAM_DURATION_MS = 30000; // 30s
    private static final int RESTART_DELAY_MS = 50; // 50ms sleep after stream close
    private File wavFile = null; // Output WAV for debugging
    private final BlockingQueue<String> transcriptionQueue = new LinkedBlockingQueue<>();
    private final SpeechClient speechClient;
    private final GrokCommandEndPoint grok;
    private final AtomicBoolean isListening = new AtomicBoolean(true);
    private long lastAudioSentTime = System.currentTimeMillis();
    private byte[] lastBuffer = null;
    private AudioInputStream audioInputStream = null;


    private Thread processingThread;

    public void stop() {
        shutdown();
        this.processingThread.stop();
    }

    public void start() {
        SystemSession.getInstance().put(SystemSession.PRIVACY_MODE, true);
        this.processingThread = new Thread(this::startStreaming);
        this.processingThread.start();
        try {
            this.grok.start();
            log.info("SpeechRecognizer started in background thread");
        } catch (Exception e) {
            log.error("Failed to initialize Grok", e);
            throw new RuntimeException(e);
        }
    }


    public SpeechRecognizer() {
        this.grok = new GrokCommandEndPoint();

        try {
            String apiKey = GoogleApiKeyProvider.getInstance().getGoogleApiKey();
            SpeechSettings settings = SpeechSettings.newBuilder()
                    .setApiKey(apiKey)
                    .build();
            this.speechClient = SpeechClient.create(settings);
            log.info("SpeechClient initialized successfully with API key");
        } catch (Exception e) {
            log.error("Failed to initialize SpeechClient: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize SpeechClient", e);
        }
    }

    public void stopWavRecording() {
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
    public void startWavRecording() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE_HERTZ, 16, 1, true, false);
            wavFile = new File("audio_debug.wav");
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, BUFFER_SIZE);
            line.start();
            audioInputStream = new AudioInputStream(line); // Fixed constructor
            new Thread(() -> {
                try {
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile); // Fixed method
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

                AudioFormat format = new AudioFormat(SAMPLE_RATE_HERTZ, 16, CHANNELS, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
                    log.info("Using streaming format: SampleRate={}, Channels={}", SAMPLE_RATE_HERTZ, CHANNELS);
                    line.open(format, BUFFER_SIZE);
                    line.start();
                    byte[] buffer = new byte[BUFFER_SIZE];

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
                        Thread.sleep(RESTART_DELAY_MS); // 1s delay before restarting
                    } catch (InterruptedException e) {
                        log.error("Restart delay interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.error("Streaming recognition failed: {}", e.getMessage());
                try {
                    Thread.sleep(RESTART_DELAY_MS); // 1s delay on failure
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
                    Object privacySystemVariable = SystemSession.getInstance().get(SystemSession.PRIVACY_MODE);
                    boolean isPrivacyModeOn = privacySystemVariable != null && (boolean) privacySystemVariable;
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
    private static StreamingRecognitionConfig getStreamingRecognitionConfig() {

        SpeechContext commandContext = SpeechContext.newBuilder()
                .addAllPhrases(GrokRequestHints.COMMON_PHRASES)
                .setBoost(35.0f)
                .build();


        // Trimmed command context with increased tritium boost (probably does not help much to be honest)
        SpeechAdaptation adaptation = SpeechAdaptation.newBuilder()
                .addPhraseSets(PhraseSet.newBuilder()
                        .setBoost(15.0f)
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("Astraea").setBoost(50.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("tritium").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("tri-tium").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("try-tee-um").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("tree-tee-um").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("trit-ee-um").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("trish-ium").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("try-tium").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("t-r-i-t-i-u-m").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("trit-ium").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("tritium fuel").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("try-tium fuel").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("hydrogen 3").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("hydrogen three").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("tritium mining").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("try-tium mining").setBoost(30.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("open cargo scoop").setBoost(25.0f))
                        .addPhrases(PhraseSet.Phrase.newBuilder()
                                .setValue("engage supercruise").setBoost(30.0f))
                        .build())
                .build();

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(SAMPLE_RATE_HERTZ)
                .setAudioChannelCount(CHANNELS)
                .setLanguageCode("en-US")
                .setEnableAutomaticPunctuation(true)
                .addSpeechContexts(commandContext)
                .setAdaptation(adaptation)
                .setModel("phone_call")
                .setEnableWordTimeOffsets(true)
                .build();

        StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(config)
                .setInterimResults(true)
                .setSingleUtterance(false)
                .build();
        return streamingConfig;
    }

    public String getNextTranscription() throws InterruptedException {
        return transcriptionQueue.take();
    }

    public void stopListening() {
        isListening.set(false);
    }

    public void shutdown() {
        stopListening();
        grok.stop();
        if (speechClient != null) {
            speechClient.close();
            log.info("SpeechClient closed");
        }
        //for debugging
        //stopWavRecording();
    }
}
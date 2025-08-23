package elite.companion.comms;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechRecognizer {
    private static final Logger log = LoggerFactory.getLogger(SpeechRecognizer.class);
    private static final int SAMPLE_RATE_HERTZ = 48000; // Match your system
    private static final int BUFFER_SIZE = 9600; // ~100ms at 48kHz mono 16-bit to avoid underruns
    private static final int KEEP_ALIVE_INTERVAL_MS = 2000; // 2 seconds
    private static final int STREAM_DURATION_MS = 240000; // 4 minutes (before 305s limit)
    private final BlockingQueue<String> transcriptionQueue = new LinkedBlockingQueue<>();
    private final SpeechClient speechClient;
    private GrokInteractionHandler grok;
    private AtomicBoolean isListening = new AtomicBoolean(true);
    private long lastAudioSentTime = System.currentTimeMillis();
    private byte[] lastBuffer = null; // For resending on restart
    private AudioInputStream audioInputStream = null; // For WAV
    private File wavFile = null; // Output WAV

    public SpeechRecognizer() {
        this.grok = new GrokInteractionHandler();
        SpeechClient tempClient = null;
        try {
            tempClient = SpeechClient.create();
            log.info("SpeechClient initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize SpeechClient", e);
            throw new RuntimeException("SpeechRecognizer initialization failed", e);
        }
        this.speechClient = tempClient;
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

    public void start() {
        new Thread(this::startStreaming).start();
        log.info("SpeechRecognizer started in background thread");
    }

    private void startStreaming() {
        while (isListening.get()) {
            log.info("Starting new streaming session...");
            long streamStartTime = System.currentTimeMillis();
            ApiStreamObserver<StreamingRecognizeRequest> requestObserver = null;

            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);

                // Command context with extra tritium focus
                SpeechContext commandContext = SpeechContext.newBuilder()
                        .addPhrases("open cargo scoop")
                        .addPhrases("close cargo scoop")
                        .addPhrases("frame shift drive")
                        .addPhrases("enter supercruise")
                        .addPhrases("engage supercruise")
                        .addPhrases("exit supercruise")
                        .addPhrases("engage ftl")
                        .addPhrases("tritium")
                        .addPhrases("TRITIUM")
                        .addPhrases("tri-tium")
                        .addPhrases("let mine some tritium")
                        .addPhrases("let get some tritium")
                        .addPhrases("set tritium as the mining target")
                        .addPhrases("let's mine some fuel")
                        .addPhrases("Alexandrite")
                        .addPhrases("Bromellite")
                        .addPhrases("Painite")
                        .addPhrases("mining")
                        .addPhrases("mine")
                        .addAllPhrases(GrokRequestHints.COMMANDS)
                        .addAllPhrases(GrokRequestHints.QUERIES)
                        .addAllPhrases(GrokRequestHints.COMMON_PHRASES)
                        .addAllPhrases(GrokRequestHints.CONCEPTS)
                        .setBoost(20.0f)
                        .build();

                // Domain terms context
                SpeechContext domainTermsContext = SpeechContext.newBuilder()
                        .addPhrases("FSD")
                        .addPhrases("SRV")
                        .addPhrases("GROK")
                        .addPhrases("Anaconda")
                        .addPhrases("Coriolis")
                        .setBoost(20.0f)
                        .build();

                RecognitionConfig config = RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setSampleRateHertz(SAMPLE_RATE_HERTZ)
                        .setLanguageCode("en-US")
                        .setEnableAutomaticPunctuation(true)
                        .addSpeechContexts(commandContext)
                        .addSpeechContexts(domainTermsContext)
                        .setModel("latest_long")
                        //.setModel("phone_call")
                        .setEnableWordTimeOffsets(true)
                        .build();

                StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                        .setConfig(config)
                        .setInterimResults(true)
                        .setSingleUtterance(false)
                        .build();

                // Initial configuration request
                List<StreamingRecognizeRequest> requests = new ArrayList<>();
                requests.add(StreamingRecognizeRequest.newBuilder()
                        .setStreamingConfig(streamingConfig)
                        .build());

                // Set up streaming observer
                requestObserver = speechClient.streamingRecognizeCallable().bidiStreamingCall(
                        new ApiStreamObserver<StreamingRecognizeResponse>() {
                            @Override
                            public void onNext(StreamingRecognizeResponse response) {
                                for (StreamingRecognitionResult result : response.getResultsList()) {
                                    if (result.getAlternativesCount() > 0) {
                                        String transcript = result.getAlternatives(0).getTranscript();
                                        if (!result.getIsFinal()) {
                                            log.debug("Interim transcript: {}", transcript);
                                        } else {
                                            transcriptionQueue.offer(transcript);
                                            log.info("Final transcript: {}", transcript);
                                            if (transcript != null && !transcript.isBlank()) {
                                                //grok.processCommand(transcript);
                                            }
                                        }
                                    }
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

                // Send initial config
                for (StreamingRecognizeRequest request : requests) {
                    requestObserver.onNext(request);
                }

                // Start audio capture
                AudioFormat format = new AudioFormat(SAMPLE_RATE_HERTZ, 16, 2, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
                    line.open(format, BUFFER_SIZE);
                    line.start();
                    // startWavRecording(); // Avoid opening a second capture line concurrently
                    byte[] buffer = new byte[BUFFER_SIZE];

                    while (isListening.get() && line.isOpen() && (System.currentTimeMillis() - streamStartTime) < STREAM_DURATION_MS) {
                        int bytesRead = line.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            byte[] trimmedBuffer = new byte[bytesRead];
                            System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
                            //double rms = calculateRMS(trimmedBuffer);
                            //log.info("RMS level: {}", rms);
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
                } catch (Exception e) {
                    log.error("Audio capture failed: {}", e.getMessage());
                }
            } catch (Exception e) {
                log.error("Streaming recognition failed: {}", e.getMessage());
            } finally {
                if (requestObserver != null) {
                    requestObserver.onCompleted();
                }
            }
        }
    }

    public String getNextTranscription() throws InterruptedException {
        return transcriptionQueue.take();
    }

    public void stopListening() {
        isListening.set(false);
    }

    public void shutdown() {
        stopListening();
        if (speechClient != null) {
            speechClient.close();
            log.info("SpeechClient closed");
        }
        stopWavRecording();
    }

    private double calculateRMS(byte[] audioData) {
        double sumSquare = 0.0;
        int sampleCount = audioData.length / 2; // 16-bit samples
        for (int i = 0; i < audioData.length; i += 2) {
            int low = audioData[i] & 0xFF;
            int high = audioData[i + 1] & 0xFF;
            short sample = (short) ((high << 8) | low);
            sumSquare += sample * sample;
        }
        return sampleCount > 0 ? Math.sqrt(sumSquare / sampleCount) : 0.0; // Avoid division by zero
    }
}
package elite.companion.comms;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static elite.companion.Globals.GOOGLE_API_KEY;

public class VoiceCommandInterpritor {

    private static final Logger log = LoggerFactory.getLogger(VoiceCommandInterpritor.class);

    private static final long LISTEN_POLL_INTERVAL_MS = 50;
    public static final int SAMPLE_RATE_HERTZ = 48000;
    private SpeechClient speechClient;


    public VoiceCommandInterpritor() {


        //TODO: Refactor this to use a config file or a user interface.
        try (InputStream sttStream = getClass().getResourceAsStream(GOOGLE_API_KEY)) {
            if (sttStream == null) {
                throw new IOException(String.format("STT service account JSON file '%s' not found in resources", GOOGLE_API_KEY));
            }
            GoogleCredentials sttCredentials = GoogleCredentials.fromStream(sttStream).createScoped("https://www.googleapis.com/auth/cloud-platform");
            SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(() -> sttCredentials).build();
            speechClient = SpeechClient.create(settings);
        } catch (Exception e) {
            log.error("Failed to initialize Speech to Text client", e);
        }
        new Thread(this::listen).start();
    }

    private void listen() {
        while (true) {
            try {
                byte[] audioData = recordAudio(); // Capture audio from your mic
                if( audioData.length == 0) continue;

                RecognizeResponse response = recognizeSpeech(audioData);
                String transcript = response.getResults(0).getAlternatives(0).getTranscript();
                log.warn("STT transcript: {}", transcript);


                if (!transcript.isEmpty()) {
                    GrokInteractionHandler processor = new GrokInteractionHandler();
                    processor.processCommand(transcript);
                }

            } catch (Exception e) {
                log.error("STT not processed: {}", e.getMessage());
            }
            if (!pauseBetweenIterations()) {
                break;
            }
        }
    }

    private boolean pauseBetweenIterations() {
        try {
            Thread.sleep(LISTEN_POLL_INTERVAL_MS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Listen loop interrupted; exiting.");
            return false;
        }
    }


    private byte[] recordAudio() throws Exception {
        // Optimized for your high-end audio setup (Shure SM7B, RME Fireface UFX+ 48kHz)
        javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(SAMPLE_RATE_HERTZ, 16, 1, true, false);
        javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, format);
        javax.sound.sampled.TargetDataLine line = (javax.sound.sampled.TargetDataLine) javax.sound.sampled.AudioSystem.getLine(info);
        line.open(format);
        line.start();

        //TODO: This should be configurable via user interface
        int durationSeconds = 10; // 10 seconds
        byte[] data = new byte[SAMPLE_RATE_HERTZ * durationSeconds * 2]; // 16-bit samples, mono
        int bytesRead = line.read(data, 0, data.length);
        line.stop();
        line.close();

        // Trim to actual bytes read
        byte[] trimmedData = new byte[bytesRead];
        System.arraycopy(data, 0, trimmedData, 0, bytesRead);

        // Silence detection: Calculate RMS
        double threshold = 550; // 5% of max 16-bit amplitude (32768)
        double rms = calculateRMS(trimmedData);
        if (rms < threshold) {
            log.debug("Silence detected, RMS: {}", rms);
            return new byte[0]; // Return empty array to indicate silence
        }

        log.debug("Audio detected, RMS: {}, bytes read: {}", rms, bytesRead);
        return trimmedData;
    }

    private double calculateRMS(byte[] audioData) {
        double sumSquare = 0.0;
        for (int i = 0; i < audioData.length; i += 2) {
            // Convert two bytes to 16-bit sample (little-endian)
            int low = audioData[i] & 0xFF;
            int high = audioData[i + 1] & 0xFF;
            short sample = (short) ((high << 8) | low);
            sumSquare += sample * sample;
        }
        int sampleCount = audioData.length / 2; // 16-bit samples
        return Math.sqrt(sumSquare / sampleCount);
    }


    private RecognizeResponse recognizeSpeech(byte[] audioData) throws Exception {
        // Domain-specific command phrases you expect
        SpeechContext commandContext = SpeechContext.newBuilder()
                .addPhrases("open cargo scoop")
                .addPhrases("close cargo scoop")
                .addPhrases("frame shift drive")
                .addPhrases("enter supercruise")
                .addPhrases("engage supercruise")
                .addPhrases("exit supercruise")
                .addPhrases("engage ftl")
                .addPhrases("tritium")
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
                .setBoost(20.0f) // Bias towards these commands
                .build();

        // Acronyms / proper nouns / product names
        SpeechContext domainTermsContext = SpeechContext.newBuilder()
                .addPhrases("FSD")
                .addPhrases("SRV")
                .addPhrases("Anaconda")
                .addPhrases("Coriolis")
                .setBoost(20.0f)
                .build();

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(SAMPLE_RATE_HERTZ)
                .setLanguageCode("en-US")
                .setEnableAutomaticPunctuation(true) // Improve natural parsing
                .addSpeechContexts(commandContext)
                .addSpeechContexts(domainTermsContext)
                .setModel("latest_long")
                .build();

        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(ByteString.copyFrom(audioData))
                .build();

        return speechClient.recognize(config, audio);
    }
}
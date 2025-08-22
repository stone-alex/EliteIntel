package elite.companion.comms;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.common.eventbus.EventBus;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static elite.companion.Globals.GOOGLE_API_KEY;

public class VoiceCommandModule {

    private static final Logger log = LoggerFactory.getLogger(VoiceCommandModule.class);

    private static final long LISTEN_POLL_INTERVAL_MS = 1000L;
    public static final int SAMPLE_RATE_HERTZ = 24000;
    private SpeechClient speechClient;


    public VoiceCommandModule() {


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
                RecognizeResponse response = recognizeSpeech(audioData);
                String transcript = response.getResults(0).getAlternatives(0).getTranscript();
                log.warn("STT transcript: {}", transcript);


                if (!transcript.isEmpty()) {
                    GrokCommandProcessor processor = new GrokCommandProcessor();
                    processor.processCommand(transcript);
                }

            } catch (Exception e) {
                log.error("STT error: {}", e.getMessage());
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
        // Optimized for your high-end audio setup (Shure SM7B, RME Fireface UFX+ 48kHz audio)
        javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(SAMPLE_RATE_HERTZ, 16, 1, true, false); // Match your TTS rate
        javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, format);
        javax.sound.sampled.TargetDataLine line = (javax.sound.sampled.TargetDataLine) javax.sound.sampled.AudioSystem.getLine(info);
        line.open(format);
        line.start();
        //TODO:This should be configurable via user interface
        byte[] data = new byte[SAMPLE_RATE_HERTZ * 20]; // 20 seconds at 24kHz or 10 seconds at 48kHz;

        int bytesRead = line.read(data, 0, data.length);
        byte[] trimmedData = new byte[bytesRead];
        System.arraycopy(data, 0, trimmedData, 0, bytesRead);
        line.stop();
        line.close();
        return trimmedData;
    }

    private RecognizeResponse recognizeSpeech(byte[] audioData) throws Exception {
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(SAMPLE_RATE_HERTZ)
                .setLanguageCode("en-US")
                .setEnableAutomaticPunctuation(true) // Improve natural parsing
                .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(ByteString.copyFrom(audioData))
                .build();
        return speechClient.recognize(config, audio);
    }
}
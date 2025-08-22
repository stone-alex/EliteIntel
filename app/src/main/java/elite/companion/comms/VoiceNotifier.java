package elite.companion.comms;

import com.google.cloud.texttospeech.v1.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static elite.companion.Globals.GOOGLE_API_KEY;

public class VoiceNotifier {
    private final TextToSpeechClient textToSpeechClient;

    public VoiceNotifier() throws IOException {
        // Load credentials from classpath
        try (InputStream serviceAccountStream = getClass().getResourceAsStream(GOOGLE_API_KEY)) {
            if (serviceAccountStream == null) {
                throw new IOException("Service account JSON file '" + GOOGLE_API_KEY + "' not found in resources");
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream).createScoped("https://www.googleapis.com/auth/cloud-platform");
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build();
            textToSpeechClient = TextToSpeechClient.create(settings);
        }
    }

    public void speak(String text) {
        try {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Wavenet-D") // Natural WaveNet voice
                    .build();
            AudioConfig config = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).setSpeakingRate(1.0).build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, config);
            byte[] audioData = response.getAudioContent().toByteArray();

            // Play audio directly using Java Sound
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(24000, 16, 1, true, false);
            javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, format);
            javax.sound.sampled.SourceDataLine line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info);
            line.open(format);


            // Pre-buffer with silence to avoid click
            byte[] silenceBuffer = new byte[2048]; // 1024 bytes of silence
            line.start();
            line.write(silenceBuffer, 0, silenceBuffer.length); // Prime the line
            line.flush(); // Clear any initial noise

            // Write the actual audio data
            line.write(audioData, 0, audioData.length);
            line.drain(); // Wait for playback to complete
            line.stop();
            line.close();

        } catch (Exception e) {
            System.err.println("TTS error: " + e.getMessage());
        }
    }

    public void close() {
        textToSpeechClient.close();
    }
}
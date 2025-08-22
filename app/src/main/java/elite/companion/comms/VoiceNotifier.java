package elite.companion.comms;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static elite.companion.Globals.GOOGLE_API_KEY;

public class VoiceNotifier {
    private static final Logger log = LoggerFactory.getLogger(VoiceNotifier.class);

    private final TextToSpeechClient textToSpeechClient;

    public VoiceNotifier()  {
        // Load credentials from classpath
        //TODO: Refactor this to use a config file or a user interface.
        try (InputStream serviceAccountStream = getClass().getResourceAsStream(GOOGLE_API_KEY)) {
            if (serviceAccountStream == null) {
                throw new IOException(String.format("Service account JSON file '%s' not found in resources", GOOGLE_API_KEY));
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream).createScoped("https://www.googleapis.com/auth/cloud-platform");
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build();
            textToSpeechClient = TextToSpeechClient.create(settings);
        } catch (Exception e) {
            log.error("Failed to initialize Text To Speech client", e);
            throw new RuntimeException("Failed to initialize Text To Speech client", e);
        }
    }

    public void speak(String text) {
        try {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Wavenet-D").build(); // Natural WaveNet voice

            AudioConfig config = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).setSpeakingRate(1.1).setSampleRateHertz(24000).build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, config);
            byte[] audioData = response.getAudioContent().toByteArray();

            // Ensure 16-bit alignment (avoid partial sample pop)
            if ((audioData.length & 1) != 0) {
                byte[] even = new byte[audioData.length - 1];
                System.arraycopy(audioData, 0, even, 0, even.length);
                audioData = even;
            }

            // Apply a short linear fade-in (~20 ms) to avoid initial discontinuity
            int fadeMs = 20;
            int samplesToFade = (24000 * fadeMs) / 1000; // 24kHz mono
            for (int i = 0; i < samplesToFade && (i * 2 + 1) < audioData.length; i++) {
                // little-endian 16-bit sample
                int lo = audioData[2 * i] & 0xFF;
                int hi = audioData[2 * i + 1] & 0xFF;
                short sample = (short) ((hi << 8) | lo);
                float gain = (float) i / samplesToFade; // 0.0 -> 1.0
                int scaled = Math.round(sample * gain);
                audioData[2 * i] = (byte) (scaled & 0xFF);
                audioData[2 * i + 1] = (byte) ((scaled >>> 8) & 0xFF);
            }

            // Play audio directly using Java Sound
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(24000, 16, 1, true, false);
            javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, format);
            javax.sound.sampled.SourceDataLine line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info);
            // Increase buffer size a bit to avoid underrun at start (e.g., ~100ms)
            int bufferBytes = (int) (format.getFrameSize() * format.getSampleRate() / 10); // ~100ms
            line.open(format, bufferBytes);

            // Pre-buffer with ~20ms of silence (do NOT flush afterward)
            int silenceFrames = (int) (format.getSampleRate() / 50); // 20 ms
            byte[] silenceBuffer = new byte[silenceFrames * format.getFrameSize()];
            line.write(silenceBuffer, 0, silenceBuffer.length); // Prime the line
            line.start();

            // Write the actual audio data
            line.write(audioData, 0, audioData.length);
            line.drain(); // Wait for playback to complete
            line.stop();
            line.close();
        } catch (Exception e) {
            log.error("Text To Speech error: {}", e.getMessage());
        }
    }
}
package elite.companion.comms.voice;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import elite.companion.session.SystemSession;
import elite.companion.util.GoogleApiKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VoiceGenerator {
    private static final Logger log = LoggerFactory.getLogger(VoiceGenerator.class);

    private final TextToSpeechClient textToSpeechClient;
    private final BlockingQueue<VoiceRequest> voiceQueue;
    private final Thread processingThread;
    private volatile boolean running;

    private final static VoiceGenerator INSTANCE = new VoiceGenerator();

    public static VoiceGenerator getInstance() {
        return INSTANCE;
    }

    private Map<String, VoiceSelectionParams> voiceMap = new HashMap<>();
    private Map<String, VoiceSelectionParams> randomVoiceMap = new HashMap<>();

    private static class VoiceRequest {
        final String text;
        final String voiceName;
        double speechRate = 1.2;

        VoiceRequest(String text, String voiceName, double speechRate) {
            this.text = text;
            this.voiceName = voiceName;
            this.speechRate = speechRate;
        }
    }

    private VoiceGenerator() {
        this.voiceQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.processingThread = new Thread(this::processVoiceQueue);
        this.processingThread.setDaemon(true);
        this.processingThread.start();

        // Load credentials using GoogleApiKeyProvider
        try (InputStream serviceAccountStream = GoogleApiKeyProvider.getInstance().getGoogleApiKeyStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            textToSpeechClient = TextToSpeechClient.create(settings);
        } catch (IOException e) {
            log.error("Failed to initialize Text To Speech client: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Text To Speech client", e);
        }

        //IMPS
        VoiceSelectionParams James = VoiceSelectionParams.newBuilder().setLanguageCode("en-AU").setName("en-AU-Chirp3-HD-Algieba").build();
        VoiceSelectionParams Charles = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Algenib").build();
        VoiceSelectionParams Jake = VoiceSelectionParams.newBuilder().setLanguageCode("en-AU").setName("en-GB-Chirp3-HD-Despina").build();
        VoiceSelectionParams Anna = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp-HD-F").build();
        VoiceSelectionParams Mary = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Neural2-A").build();
        VoiceSelectionParams Betty = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Aoede").build();
        VoiceSelectionParams Olivia = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Aoede").build();

        //FEDS
        VoiceSelectionParams Michael = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Charon").build();
        VoiceSelectionParams Steve = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Algenib").build();
        VoiceSelectionParams Joseph = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Sadachbia").build();
        VoiceSelectionParams Jennifer = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Sulafat").build();
        VoiceSelectionParams Rachel = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Zephyr").build();
        VoiceSelectionParams Karen = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Despina").build();
        VoiceSelectionParams Emma = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Despina").build();

        voiceMap.put(Voices.ANNA.getName(), Anna);
        voiceMap.put(Voices.CHARLES.getName(), Charles);
        voiceMap.put(Voices.JAMES.getName(), James);
        voiceMap.put(Voices.JENNIFER.getName(), Jennifer);
        voiceMap.put(Voices.JOSEPH.getName(), Joseph);
        voiceMap.put(Voices.KAREN.getName(), Karen);
        voiceMap.put(Voices.JAKE.getName(), Jake);
        voiceMap.put(Voices.MARY.getName(), Mary);
        voiceMap.put(Voices.MICHAEL.getName(), Michael);
        voiceMap.put(Voices.RACHEL.getName(), Rachel);
        voiceMap.put(Voices.STEVE.getName(), Steve);
        voiceMap.put(Voices.BETTY.getName(), Betty);
        voiceMap.put(Voices.EMMA.getName(), Emma);
        voiceMap.put(Voices.OLIVIA.getName(), Olivia);

        randomVoiceMap.put(Voices.ANNA.getName(), Anna);
        randomVoiceMap.put(Voices.CHARLES.getName(), Charles);
        randomVoiceMap.put(Voices.JAMES.getName(), James);
        randomVoiceMap.put(Voices.JENNIFER.getName(), Jennifer);
        randomVoiceMap.put(Voices.JOSEPH.getName(), Joseph);
        randomVoiceMap.put(Voices.KAREN.getName(), Karen);
        randomVoiceMap.put(Voices.JAKE.getName(), Jake);
        randomVoiceMap.put(Voices.MARY.getName(), Mary);
        randomVoiceMap.put(Voices.MICHAEL.getName(), Michael);
        randomVoiceMap.put(Voices.RACHEL.getName(), Rachel);
        randomVoiceMap.put(Voices.STEVE.getName(), Steve);
        randomVoiceMap.put(Voices.BETTY.getName(), Betty);
        randomVoiceMap.put(Voices.EMMA.getName(), Emma);
        randomVoiceMap.put(Voices.OLIVIA.getName(), Olivia);
    }



    public void speakInRandomVoice(String text) {
        if(text == null || text.isEmpty()) return;
        speak(text, getRandomVoice());
    }

    public Voices getRandomVoice() {
        if (voiceMap.isEmpty()) {
            return SystemSession.getInstance().getAIVoice();
        }
        Voices[] voices = randomVoiceMap.keySet().toArray(new Voices[0]);
        return voices[new Random().nextInt(voices.length)];
    }

    public void speak(String text) {
        if(text == null || text.isEmpty()) return;

        new Thread(() -> speak(text, SystemSession.getInstance().getAIVoice())).start();
    }

    public void speak(String text, Voices aiVoice) {
        if(text == null || text.isEmpty()) return;
        try {
            voiceQueue.put(new VoiceRequest(text, aiVoice.getName(), aiVoice.getSpeechRate()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while adding voice request to queue", e);
        }
    }

    private void processVoiceQueue() {
        while (running) {
            try {
                VoiceRequest request = voiceQueue.take();
                processVoiceRequest(request.text, request.voiceName, request.speechRate);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Voice processing thread interrupted", e);
                break;
            }
        }
    }

    private void processVoiceRequest(String text, String voiceName, double speechRate) {
        if (text == null || text.isEmpty()) {return;}
        log.info(voiceName + " Speaking: {}", text);
        //SessionTracker.getInstance().updateSession("context_your_last_transmission", "Timestamp:"+ Instant.now().toString()+" text: " + text);
        try {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = voiceMap.get(voiceName);

            AudioConfig config = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).setSpeakingRate(speechRate).setSampleRateHertz(24000).build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, config);
            byte[] audioData = response.getAudioContent().toByteArray();

            // Ensure 16-bit alignment (avoid partial sample pop)
            if ((audioData.length & 1) != 0) {
                byte[] even = new byte[audioData.length - 1];
                System.arraycopy(audioData, 0, even, 0, even.length);
                audioData = even;
            }

            // Apply a short linear fade-in (~20 ms) to avoid initial discontinuity
            applyFadeIn(audioData, 20);

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

    private static void applyFadeIn(byte[] audioData, int fadeMs) {
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
    }
}
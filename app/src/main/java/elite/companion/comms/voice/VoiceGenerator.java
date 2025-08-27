package elite.companion.comms.voice;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static elite.companion.Globals.GOOGLE_API_KEY;

public class VoiceGenerator {
    private static final Logger log = LoggerFactory.getLogger(VoiceGenerator.class);
    public static final String JAMES = "James";
    public static final String MICHEAL = "Micheal";
    public static final String JENNIFER = "Jennifer";
    public static final String MERRY = "Merry";
    public static final String FAST_JENNY = "Fast Jenny";
    public static final String CHARLES = "Charles";
    public static final String STEVE = "Steve";
    public static final String JOSEPH = "Joseph";
    public static final String LEDA = "Leda";
    public static final String KAREN = "Karen";
    public static final String ELYRIA = "Elyria";

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

        VoiceRequest(String text, String voiceName) {
            this.text = text;
            this.voiceName = voiceName;
        }
    }

    private VoiceGenerator() {
        this.voiceQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.processingThread = new Thread(this::processVoiceQueue);
        this.processingThread.setDaemon(true);
        this.processingThread.start();
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

        VoiceSelectionParams James = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Wavenet-D").build();
        VoiceSelectionParams Micheal = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Algieba").build();
        VoiceSelectionParams Jennifer = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Sulafat").build();
        VoiceSelectionParams Merry = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Neural2-A").build();
        VoiceSelectionParams FastJenny = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp-HD-F").build();
        VoiceSelectionParams Charles = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Algenib").build();
        VoiceSelectionParams Steven = VoiceSelectionParams.newBuilder().setLanguageCode("en-AU").setName("en-AU-Chirp3-HD-Schedar").build();
        VoiceSelectionParams Joseph = VoiceSelectionParams.newBuilder().setLanguageCode("en-AU").setName("en-AU-Chirp3-HD-Enceladus").build();
        VoiceSelectionParams Leda = VoiceSelectionParams.newBuilder().setLanguageCode("en-AU").setName("en-AU-Chirp3-HD-Leda").build();
        VoiceSelectionParams Karen = VoiceSelectionParams.newBuilder().setLanguageCode("en-AU").setName("en-AU-Chirp3-HD-Gacrux").build();
        VoiceSelectionParams Elyria = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Standard-A").build();

        voiceMap.put(JAMES, James);
        voiceMap.put(MICHEAL, Micheal);
        voiceMap.put(JENNIFER, Jennifer);

        voiceMap.put(ELYRIA, Elyria);
        voiceMap.put(MERRY, Merry);
        voiceMap.put(FAST_JENNY, FastJenny);
        voiceMap.put(CHARLES, Charles);

        voiceMap.put(STEVE, Steven);
        voiceMap.put(JOSEPH, Joseph);
        voiceMap.put(LEDA, Leda);
        voiceMap.put(KAREN, Karen);

        randomVoiceMap.put(JAMES, James);
        randomVoiceMap.put(MICHEAL, Micheal);
        randomVoiceMap.put(JENNIFER, Jennifer);
        randomVoiceMap.put(MERRY, Merry);
        randomVoiceMap.put(ELYRIA, Elyria);
        randomVoiceMap.put(FAST_JENNY, FastJenny);
        //randomVoiceMap.put(CHARLES, Charles);
        randomVoiceMap.put(STEVE, Steven);
        randomVoiceMap.put(JOSEPH, Joseph);
        randomVoiceMap.put(LEDA, Leda);
        randomVoiceMap.put(KAREN, Karen);
    }



    public void speakInRandomVoice(String text) {
        if(text == null || text.isEmpty()) return;
        speak(text, getRandomVoice());
    }

    public String getRandomVoice() {
        if (voiceMap.isEmpty()) {
            return CHARLES;
        }
        String[] voices = randomVoiceMap.keySet().toArray(new String[0]);
        return voices[new Random().nextInt(voices.length)];
    }

    public void speak(String text) {
        if(text == null || text.isEmpty()) return;
        speak(text, CHARLES);
    }

    public void speak(String text, String voiceName) {
        if(text == null || text.isEmpty()) return;
        try {
            voiceQueue.put(new VoiceRequest(text, voiceName));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while adding voice request to queue", e);
        }
    }

    private void processVoiceQueue() {
        while (running) {
            try {
                VoiceRequest request = voiceQueue.take();
                processVoiceRequest(request.text, request.voiceName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Voice processing thread interrupted", e);
                break;
            }
        }
    }

    private void processVoiceRequest(String text, String voiceName) {
        if (text == null || text.isEmpty()) {return;}
        log.info("Speaking: {}", text);
        //SessionTracker.getInstance().updateSession("context_your_last_transmission", "Timestamp:"+ Instant.now().toString()+" text: " + text);
        try {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = voiceMap.get(voiceName);

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
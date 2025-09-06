package elite.companion.comms.mouth.google;

import com.google.cloud.texttospeech.v1.*;
import com.google.common.eventbus.Subscribe;
import elite.companion.comms.ConfigManager;
import elite.companion.comms.mouth.GoogleVoices;
import elite.companion.comms.mouth.MouthInterface;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the MouthInterface that utilizes Google Cloud Text-to-Speech API
 * to generate human-like speech audio from text. This class contains methods for initializing
 * and managing the Text-to-Speech client, processing voice requests, and handling events
 * related to text-to-speech actions.
 *
 * <p>The class maintains a queue system for handling multiple voice requests asynchronously.
 * It also supports selecting different voices for speech synthesis and provides functionality
 * for managing the lifecycle of the text-to-speech processing thread.
 * <p>
 * This class implements the Singleton design pattern, allowing one instance to manage
 * all text-to-speech operations within the system.
 */
public class GoogleTTSImpl implements MouthInterface {
    private static final Logger log = LoggerFactory.getLogger(GoogleTTSImpl.class);

    private TextToSpeechClient textToSpeechClient;
    private final BlockingQueue<VoiceRequest> voiceQueue;
    private Thread processingThread;
    private volatile boolean running;

    private static final GoogleTTSImpl INSTANCE = new GoogleTTSImpl();

    public static GoogleTTSImpl getInstance() {
        return INSTANCE;
    }

    private final Map<String, VoiceSelectionParams> voiceMap = new HashMap<>();
    private final Map<String, VoiceSelectionParams> randomVoiceMap = new HashMap<>();

    private static class VoiceRequest {
        final String text;
        final String voiceName;
        final double speechRate;

        VoiceRequest(String text, String voiceName, double speechRate) {
            this.text = text;
            this.voiceName = voiceName;
            this.speechRate = speechRate;
        }
    }

    private GoogleTTSImpl() {
        EventBusManager.register(this);
        this.voiceQueue = new LinkedBlockingQueue<>();


        // Initialize voice mappings
        VoiceSelectionParams James = VoiceSelectionParams.newBuilder().setLanguageCode("en-AU").setName("en-AU-Chirp3-HD-Algieba").build();
        VoiceSelectionParams Charles = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Algenib").build();
        VoiceSelectionParams Jake = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Iapetus").build();
        VoiceSelectionParams Anna = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp-HD-F").build();
        VoiceSelectionParams Mary = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Neural2-A").build();
        VoiceSelectionParams Betty = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Aoede").build();
        VoiceSelectionParams Olivia = VoiceSelectionParams.newBuilder().setLanguageCode("en-GB").setName("en-GB-Chirp3-HD-Aoede").build();
        VoiceSelectionParams Michael = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Charon").build();
        VoiceSelectionParams Steve = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Algenib").build();
        VoiceSelectionParams Joseph = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Sadachbia").build();
        VoiceSelectionParams Jennifer = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Sulafat").build();
        VoiceSelectionParams Rachel = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Zephyr").build();
        VoiceSelectionParams Karen = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Despina").build();
        VoiceSelectionParams Emma = VoiceSelectionParams.newBuilder().setLanguageCode("en-US").setName("en-US-Chirp3-HD-Despina").build();

        voiceMap.put(GoogleVoices.ANNA.getName(), Anna);
        voiceMap.put(GoogleVoices.CHARLES.getName(), Charles);
        voiceMap.put(GoogleVoices.JAMES.getName(), James);
        voiceMap.put(GoogleVoices.JENNIFER.getName(), Jennifer);
        voiceMap.put(GoogleVoices.JOSEPH.getName(), Joseph);
        voiceMap.put(GoogleVoices.KAREN.getName(), Karen);
        voiceMap.put(GoogleVoices.JAKE.getName(), Jake);
        voiceMap.put(GoogleVoices.MARY.getName(), Mary);
        voiceMap.put(GoogleVoices.MICHAEL.getName(), Michael);
        voiceMap.put(GoogleVoices.RACHEL.getName(), Rachel);
        voiceMap.put(GoogleVoices.STEVE.getName(), Steve);
        voiceMap.put(GoogleVoices.BETTY.getName(), Betty);
        voiceMap.put(GoogleVoices.EMMA.getName(), Emma);
        voiceMap.put(GoogleVoices.OLIVIA.getName(), Olivia);

        randomVoiceMap.put(GoogleVoices.ANNA.getName(), Anna);
        randomVoiceMap.put(GoogleVoices.CHARLES.getName(), Charles);
        randomVoiceMap.put(GoogleVoices.JAMES.getName(), James);
        randomVoiceMap.put(GoogleVoices.JENNIFER.getName(), Jennifer);
        randomVoiceMap.put(GoogleVoices.JOSEPH.getName(), Joseph);
        randomVoiceMap.put(GoogleVoices.KAREN.getName(), Karen);
        randomVoiceMap.put(GoogleVoices.JAKE.getName(), Jake);
        randomVoiceMap.put(GoogleVoices.MARY.getName(), Mary);
        randomVoiceMap.put(GoogleVoices.MICHAEL.getName(), Michael);
        randomVoiceMap.put(GoogleVoices.RACHEL.getName(), Rachel);
        randomVoiceMap.put(GoogleVoices.STEVE.getName(), Steve);
        randomVoiceMap.put(GoogleVoices.BETTY.getName(), Betty);
        randomVoiceMap.put(GoogleVoices.EMMA.getName(), Emma);
        randomVoiceMap.put(GoogleVoices.OLIVIA.getName(), Olivia);
    }

    @Override public synchronized void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("VoiceGenerator is already running");
            return;
        }

        TextToSpeechClient client;
        try {
            String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.TTS_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("Google API key not found in system.conf");
                return;
            }
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setApiKey(apiKey).build();
            client = TextToSpeechClient.create(settings);
            log.info("TextToSpeechClient initialized successfully with API key");
        } catch (Exception e) {
            log.error("Failed to initialize TextToSpeechClient: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize TextToSpeechClient", e);
        }
        this.textToSpeechClient = client;

        running = true;
        processingThread = new Thread(this::processVoiceQueue, "VoiceGeneratorThread");
        processingThread.start();
        log.info("VoiceGenerator started");
    }

    @Override public synchronized void stop() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("VoiceGenerator is not running");
            return;
        }
        running = false;
        processingThread.interrupt();
        try {
            processingThread.join(5000); // Wait up to 5 seconds for clean shutdown
            log.info("VoiceGenerator stopped");
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for VoiceGenerator to stop", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        try {
            textToSpeechClient.close();
            log.info("TextToSpeechClient closed");
        } catch (Exception e) {
            log.error("Failed to close TextToSpeechClient", e);
        }
        processingThread = null;
    }

    private GoogleVoices getRandomVoice() {
        if (voiceMap.isEmpty()) {
            return SystemSession.getInstance().getAIVoice();
        }
        GoogleVoices[] voices = randomVoiceMap.keySet().toArray(new GoogleVoices[0]);
        return voices[new Random().nextInt(voices.length)];
    }

    @Subscribe @Override public void onVoiceProcessEvent(VoiceProcessEvent event) {
        if (event.isUseRandom()) {
            speak(event.getText(), getRandomVoice());
        } else {
            speak(event.getText());
        }
    }

    private void speak(String text) {
        if (text == null || text.isEmpty()) return;
        if (text.toLowerCase().contains("moment")) return;

        new Thread(() -> speak(text, SystemSession.getInstance().getAIVoice())).start();
    }

    private void speak(String text, GoogleVoices aiVoice) {
        if (text == null || text.isEmpty()) return;
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
                VoiceRequest request = voiceQueue.poll(1, TimeUnit.SECONDS);
                if (request == null) {
                    if (Thread.currentThread().isInterrupted() || !running) {
                        log.info("Shutting down VoiceGenerator due to interruption or stop signal");
                        return;
                    }
                    continue;
                }
                processVoiceRequest(request.text, request.voiceName, request.speechRate);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("VoiceGenerator interrupted, shutting down");
                return;
            } catch (Exception e) {
                log.error("Unexpected error in VoiceGenerator", e);
            }
        }
    }

    private void processVoiceRequest(String text, String voiceName, double speechRate) {
        if (text == null || text.isEmpty()) {
            return;
        }
        log.info("{} Speaking: {}", voiceName, text);
        try {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = voiceMap.get(voiceName);
            if (voice == null) {
                log.warn("No voice found for name: {}, using default", voiceName);
                return;
            }

            AudioConfig config = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(speechRate)
                    .setSampleRateHertz(24000)
                    .build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, config);
            byte[] audioData = response.getAudioContent().toByteArray();

            // Ensure 16-bit alignment (avoid partial sample pop)
            if ((audioData.length & 1) != 0) {
                byte[] even = new byte[audioData.length - 1];
                System.arraycopy(audioData, 0, even, 0, even.length);
                audioData = even;
            }

            // Apply a short linear fade-in and fade out (~20 ms) to avoid initial discontinuity
            applyFade(audioData, 20, true);
            applyFade(audioData, 20, false);

            // Play audio directly using Java Sound
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(24000, 16, 1, true, false);
            javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, format);
            try (javax.sound.sampled.SourceDataLine line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info)) {
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
            }
        } catch (Exception e) {
            log.error("Text-to-speech error: {}", e.getMessage(), e);
        }
    }

    private static void applyFade(byte[] audioData, int fadeMs, boolean isFadeIn) {
        int samplesToFade = (24000 * fadeMs) / 1000; // 24kHz mono
        int startIndex = isFadeIn ? 0 : Math.max(0, audioData.length / 2 - samplesToFade);
        for (int i = startIndex; i < startIndex + samplesToFade && (i * 2 + 1) < audioData.length; i++) {
            int lo = audioData[2 * i] & 0xFF;
            int hi = audioData[2 * i + 1] & 0xFF;
            short sample = (short) ((hi << 8) | lo);
            float gain = isFadeIn ? (float) i / samplesToFade : (float) (startIndex + samplesToFade - i) / samplesToFade;
            int scaled = Math.round(sample * gain);
            audioData[2 * i] = (byte) (scaled & 0xFF);
            audioData[2 * i + 1] = (byte) ((scaled >>> 8) & 0xFF);
        }
    }
}
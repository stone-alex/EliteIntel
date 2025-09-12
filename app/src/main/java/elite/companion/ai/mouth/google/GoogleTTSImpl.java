package elite.companion.ai.mouth.google;

import com.google.cloud.texttospeech.v1.*;
import com.google.common.eventbus.Subscribe;
import elite.companion.ai.ConfigManager;
import elite.companion.ai.ears.TTSPlaybackStartEvent;
import elite.companion.ai.mouth.AiVoices;
import elite.companion.ai.mouth.MouthInterface;
import elite.companion.ai.mouth.TTSInterruptEvent;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    private final GoogleVoiceProvider googleVoiceProvider;
    private final AtomicBoolean interruptRequested = new AtomicBoolean(false);
    private final AtomicReference<SourceDataLine> currentLine = new AtomicReference<>();

    private static final GoogleTTSImpl INSTANCE = new GoogleTTSImpl();

    public static GoogleTTSImpl getInstance() {
        return INSTANCE;
    }

    private static class VoiceRequest {
        private final String text;
        private final String voiceName;
        private final double speechRate;

        VoiceRequest(String text, String voiceName, double speechRate) {
            this.text = text;
            this.voiceName = voiceName;
            this.speechRate = speechRate;
        }

        public String getText() {
            return text;
        }

        public String getVoiceName() {
            return voiceName;
        }

        public double getSpeechRate() {
            return speechRate;
        }
    }

    private GoogleTTSImpl() {
        EventBusManager.register(this);
        this.voiceQueue = new LinkedBlockingQueue<>();
        googleVoiceProvider = GoogleVoiceProvider.getInstance();
    }

    @Override
    public synchronized void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("VoiceGenerator is already running");
            return;
        }

        try {
            String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.TTS_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("TTS API key not found in system.conf");
                return;
            }
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setApiKey(apiKey).build();
            textToSpeechClient = TextToSpeechClient.create(settings);
            log.info("TextToSpeechClient initialized successfully with API key");
        } catch (Exception e) {
            log.error("Failed to initialize TextToSpeechClient: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize TextToSpeechClient", e);
        }

        running = true;
        processingThread = new Thread(this::processVoiceQueue, "VoiceGeneratorThread");
        processingThread.start();
        log.info("VoiceGenerator started");
    }

    @Override
    public synchronized void stop() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("VoiceGenerator is not running");
            return;
        }
        running = false;
        interruptAndClear();
        processingThread.interrupt();
        try {
            processingThread.join(5000);
            log.info("VoiceGenerator stopped");
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for VoiceGenerator to stop", e);
            Thread.currentThread().interrupt();
        }
        try {
            if (textToSpeechClient != null) {
                textToSpeechClient.close();
                log.info("TextToSpeechClient closed");
            }
        } catch (Exception e) {
            log.error("Failed to close TextToSpeechClient", e);
        }
        processingThread = null;
        textToSpeechClient = null;
    }

    @Override
    public synchronized void interruptAndClear() {
        voiceQueue.clear();
        interruptRequested.set(true);
        SourceDataLine line = currentLine.get();
        if (line != null) {
            line.stop();
            line.close();
            currentLine.set(null);
        }
        interruptRequested.set(false); // Reset flag to allow new vocalizations
        log.info("TTS interrupted and queue cleared, thread alive={}, interruptRequested={}",
                processingThread != null && processingThread.isAlive(), interruptRequested.get());
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("Processing thread stopped unexpectedly, restarting");
            start();
        }
    }

    @Subscribe
    public void onInterruptEvent(TTSInterruptEvent event) {
        interruptAndClear();
    }

    @Subscribe
    @Override
    public void onVoiceProcessEvent(VoiceProcessEvent event) {
        log.debug("Received VoiceProcessEvent: text='{}', useRandom={}", event.getText(), event.isUseRandom());
        if (event.isUseRandom()) {
            speak(event.getText(), googleVoiceProvider.getRandomVoice());
        } else {
            speak(event.getText(), googleVoiceProvider.getUserSelectedVoice());
        }
    }

    private void speak(String text, AiVoices aiVoice) {
        if (text == null || text.isEmpty()) {
            return;
        }
        try {
            voiceQueue.put(new VoiceRequest(text, aiVoice.getName(), aiVoice.getSpeechRate()));
            log.debug("Added VoiceRequest to queue: text='{}', voice='{}'", text, aiVoice.getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while adding voice request to queue", e);
        }
    }

    private void processVoiceQueue() {
        while (running) {
            try {
                log.trace("Polling voice queue, size={}, interruptRequested={}",
                        voiceQueue.size(), interruptRequested.get());
                VoiceRequest request = voiceQueue.poll(1, TimeUnit.SECONDS);
                if (request == null) {
                    if (Thread.currentThread().isInterrupted() || !running) {
                        log.info("Shutting down VoiceGenerator due to interruption or stop signal");
                        return;
                    }
                    continue;
                }
                processVoiceRequest(request.getText(), request.getVoiceName(), request.getSpeechRate());
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
        long startTime = System.currentTimeMillis();
        Thread currentThread = Thread.currentThread();
        log.debug("Processing VoiceRequest: text='{}', voice='{}', threadName='{}', threadId={}, threadState={}",
                text, voiceName, currentThread.getName(), currentThread.getId(), currentThread.getState());

        try {
            if (textToSpeechClient == null) {
                log.error("TextToSpeechClient is null, restarting service");
                synchronized (this) {
                    start();
                }
                if (textToSpeechClient == null) {
                    log.error("Failed to reinitialize TextToSpeechClient, skipping request");
                    return;
                }
            }

            VoiceSelectionParams voice = googleVoiceProvider.getVoiceParams(voiceName);
            if (voice == null) {
                log.warn("No voice found for name: {}, using default", voiceName);
                voice = googleVoiceProvider.getVoiceParams(AiVoices.JENNIFER.getName());
            }

            if (interruptRequested.get()) {
                log.debug("Request interrupted before synthesis, skipping: {}", text);
                return;
            }

            log.debug("Calling Google TTS API");
            long apiStartTime = System.currentTimeMillis();
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            AudioConfig config = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(speechRate)
                    .setVolumeGainDb(-6) // set volume to -6 dbFS to prevent excessive loudness and clipping. (could be a UI setting)
                    //.setSampleRateHertz(24000) // optional wrong value may reduce quality.
                    .build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, config);
            long apiEndTime = System.currentTimeMillis();
            log.debug("Google TTS API call completed in {}ms", apiEndTime - apiStartTime);

            byte[] audioData = response.getAudioContent().toByteArray();
            if ((audioData.length & 1) != 0) {
                byte[] even = new byte[audioData.length - 1];
                System.arraycopy(audioData, 0, even, 0, even.length);
                audioData = even;
            }
            applyFade(audioData, 20, true);
            applyFade(audioData, 20, false);

            log.debug("Opening SourceDataLine");
            long lineOpenStartTime = System.currentTimeMillis();
            AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            int bufferBytes = (int) (format.getFrameSize() * format.getSampleRate() / 10);
            int silenceFrames = (int) (format.getSampleRate() / 50);
            byte[] silenceBuffer = new byte[silenceFrames * format.getFrameSize()];
            int chunkSize = bufferBytes;

            currentLine.set(null);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format, bufferBytes);
                currentLine.set(line);
                log.debug("SourceDataLine opened in {}ms", System.currentTimeMillis() - lineOpenStartTime);

                log.debug("Starting playback");
                line.write(silenceBuffer, 0, silenceBuffer.length);
                line.start();
                log.info("Spoke with voice {}: {}", voiceName, text);
                EventBusManager.publish(new TTSPlaybackStartEvent(text));

                long writeStartTime = System.currentTimeMillis();
                for (int i = 0; i < audioData.length; i += chunkSize) {
                    if (interruptRequested.get()) {
                        log.debug("Playback interrupted mid-stream: {}", text);
                        break;
                    }
                    int len = Math.min(chunkSize, audioData.length - i);
                    line.write(audioData, i, len);
                }
                if (interruptRequested.get()) {
                    line.flush();
                    log.debug("Playback interrupted");
                } else {
                    line.drain();
                }
                log.debug("Audio playback completed in {}ms", System.currentTimeMillis() - writeStartTime);
            } catch (LineUnavailableException e) {
                log.error("Audio device unavailable, possible contention: {}", e.getMessage(), e);
                try {
                    log.debug("Retrying SourceDataLine open");
                    SourceDataLine retryLine = (SourceDataLine) AudioSystem.getLine(info);
                    retryLine.open(format, bufferBytes);
                    currentLine.set(retryLine);
                    retryLine.write(silenceBuffer, 0, silenceBuffer.length);
                    retryLine.start();
                    for (int i = 0; i < audioData.length; i += chunkSize) {
                        if (interruptRequested.get()) {
                            log.debug("Playback interrupted mid-stream on retry: {}", text);
                            break;
                        }
                        int len = Math.min(chunkSize, audioData.length - i);
                        retryLine.write(audioData, i, len);
                    }
                    if (interruptRequested.get()) {
                        retryLine.flush();
                    } else {
                        retryLine.drain();
                    }
                    retryLine.close();
                    log.info("Audio playback retried successfully");
                } catch (LineUnavailableException retryEx) {
                    log.error("Retry failed for audio device: {}", retryEx.getMessage(), retryEx);
                }
            }
        } catch (Exception e) {
            log.error("Text-to-speech error: {}", e.getMessage(), e);
        } finally {
            currentLine.set(null);
            interruptRequested.set(false);
        }
        log.debug("VoiceRequest processing completed in {}ms", System.currentTimeMillis() - startTime);
    }

    private static void applyFade(byte[] audioData, int fadeMs, boolean isFadeIn) {
        int samplesToFade = (24000 * fadeMs) / 1000;
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
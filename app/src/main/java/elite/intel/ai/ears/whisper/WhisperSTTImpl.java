package elite.intel.ai.ears.whisper;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ears.*;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.STTSanitizer;
import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperJNI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class WhisperSTTImpl implements EarsInterface {

    private static final Logger log = LogManager.getLogger(WhisperSTTImpl.class);
    private static final int WHISPER_SAMPLE_RATE = 16000; // Whisper requires exactly 16kHz
    private static final int CHANNELS = 1;
    private static final int ENTER_VOICE_FRAMES = 1;
    private static final int EXIT_SILENCE_FRAMES = 5; // ~1s silence at 100ms buffers
    private static final long BASE_BACKOFF_MS = 2000;
    private static final long MAX_BACKOFF_MS = 60000;
    private static final int MIN_AUDIO_MS = 2000; // pad to at least 1 second
    private static final int MIN_AUDIO_BYTES = WHISPER_SAMPLE_RATE * 2 * MIN_AUDIO_MS / 1000; // 32000 bytes


    private final AtomicBoolean isListening = new AtomicBoolean(false);
    private final AtomicBoolean isSpeaking = new AtomicBoolean(false);
    private final SystemSession systemSession = SystemSession.getInstance();
    private final ByteArrayOutputStream audioCollector = new ByteArrayOutputStream();

    private WhisperJNI whisper;
    private WhisperContext whisperCtx;
    private Resampler resampler;
    private int sampleRateHertz;
    private int bufferSize;
    private double RMS_THRESHOLD_HIGH;
    private Thread processingThread;

    public WhisperSTTImpl() {
        EventBusManager.register(this);
    }

    @Override
    public void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("Whisper STT already running");
            return;
        }

        AudioFormatDetector.Format format = AudioFormatDetector.detectSupportedFormat();
        this.sampleRateHertz = format.getSampleRate();
        this.bufferSize = format.getBufferSize();

        Double high = systemSession.getRmsThresholdHigh();
        Double low = systemSession.getRmsThresholdLow();
        if (high == 0 || low == 0) {
            RmsTupple<Double, Double> cal = AudioCalibrator.calibrateRMS(sampleRateHertz, bufferSize);
            this.RMS_THRESHOLD_HIGH = cal.getRmsHigh();
        } else {
            this.RMS_THRESHOLD_HIGH = high;
        }

        try {
            WhisperJNI.loadLibrary();
            whisper = new WhisperJNI();
            String modelPath = systemSession.getWhisperModelPath(); // new session field
            whisperCtx = whisper.init(Path.of(modelPath));
            log.info("Whisper context loaded from {}", modelPath);
        } catch (Exception e) {
            log.error("Failed to initialize Whisper: {}", e.getMessage());
            throw new RuntimeException("Whisper init failed", e);
        }

        isListening.set(true);
        processingThread = new Thread(this::captureLoop);
        processingThread.start();
        EventBusManager.publish(new AiVoxResponseEvent("Voice input enabled"));
    }

    @Override
    public void stop() {
        isListening.set(false);
        if (processingThread != null) processingThread.interrupt();
        if (whisperCtx != null) whisperCtx.close();
        EventBusManager.publish(new AiVoxResponseEvent("Voice input disabled."));
    }

    private void captureLoop() {
        if (sampleRateHertz != WHISPER_SAMPLE_RATE) {
            resampler = new Resampler(sampleRateHertz, WHISPER_SAMPLE_RATE, CHANNELS);
            log.info("Resampling {} → {} Hz", sampleRateHertz, WHISPER_SAMPLE_RATE);
        }

        int retryCount = 0;

        while (isListening.get()) {
            try {
                runVadAndTranscribe();
                retryCount = 0;
            } catch (LineUnavailableException e) {
                log.error("Audio line unavailable: {}", e.getMessage());
                retryWithBackoff(retryCount++);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Capture loop error: {}", e.getMessage(), e);
                retryWithBackoff(retryCount++);
            }
        }
    }

    private void runVadAndTranscribe() throws LineUnavailableException, InterruptedException {
        AudioFormat audioFormat = new AudioFormat(sampleRateHertz, 16, CHANNELS, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            line.open(audioFormat, bufferSize);
            line.start();
            byte[] buffer = new byte[bufferSize];

            boolean isActive = false;
            int consecutiveVoice = 0;
            int consecutiveSilence = 0;
            audioCollector.reset();

            while (isListening.get() && line.isOpen()) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead <= 0) continue;

                byte[] audio = (resampler != null)
                        ? resampler.resample(buffer, bytesRead)
                        : buffer;
                int audioLen = (resampler != null) ? audio.length : bytesRead;

                double rms = calculateRMS(audio, audioLen);

                if (rms > RMS_THRESHOLD_HIGH) {
                    consecutiveVoice++;
                    consecutiveSilence = 0;
                    audio = Amplifier.amplify(audio, 3.0);
                } else {
                    consecutiveVoice = 0;
                    consecutiveSilence++;
                }

                if (!isActive && consecutiveVoice >= ENTER_VOICE_FRAMES && !isSpeaking.get()) {
                    isActive = true;
                    audioCollector.reset();
                    log.debug("VAD: speech started");
                }

                boolean wasActive = isActive;

                if (isActive && consecutiveSilence >= EXIT_SILENCE_FRAMES) {
                    isActive = false;
                    log.debug("VAD: speech ended");
                }

                if (isActive && consecutiveSilence == 0) {
                    audioCollector.write(audio, 0, audioLen);
                }

                if (wasActive && !isActive && audioCollector.size() > 0) {
                    final byte[] utterance = audioCollector.toByteArray();
                    audioCollector.reset();
                    Thread.ofVirtual().start(() -> transcribeAndDispatch(utterance));
                }
            }
        }
    }

    private byte[] padAudio(byte[] pcm) {
        if (pcm.length >= MIN_AUDIO_BYTES) return pcm;
        byte[] padded = new byte[MIN_AUDIO_BYTES];
        System.arraycopy(pcm, 0, padded, 0, pcm.length);
        // remaining bytes are already zero (silence) by default
        return padded;
    }

    private synchronized void transcribeAndDispatch(byte[] pcmBytes) {
        try {
            float[] samples = pcm16ToFloat(padAudio(pcmBytes));

            WhisperFullParams params = new WhisperFullParams();
            params.language = "en";
            params.nThreads = Math.min(Runtime.getRuntime().availableProcessors(), 4);
            params.noContext = true;      // Don't carry context between utterances
            params.singleSegment = false;
            long timeStart = System.currentTimeMillis();
            int result = whisper.full(whisperCtx, params, samples, samples.length);
            if (result != 0) {
                log.warn("Whisper transcription returned error code {}", result);
                return;
            }

            StringBuilder sb = new StringBuilder();
            int segments = whisper.fullNSegments(whisperCtx);
            for (int i = 0; i < segments; i++) {
                sb.append(whisper.fullGetSegmentText(whisperCtx, i));
            }
            long timeEnd = System.currentTimeMillis();

            long timeElapsed = timeEnd - timeStart;
            log.debug("Whisper input: {} bytes = {}ms of audio",
                    pcmBytes.length,
                    (pcmBytes.length / 2) * 1000 / WHISPER_SAMPLE_RATE);
            log.debug("Whisper using {} threads", Runtime.getRuntime().availableProcessors());
            log.debug("Whisper transcription took {} ms", timeElapsed);


            String transcript = sb.toString().toLowerCase().trim().replace("[blank_audio]", "");
            if (transcript.contains("(")) return;
            if (transcript.contains("*")) return;
            if (transcript.contains("[")) return;
            if (transcript.isBlank() || transcript.length() < 3) return;

            EventBusManager.publish(new AppLogEvent("STT Heard: [" + transcript + "]"));
            String sanitized = STTSanitizer.getInstance().correctMistakes(transcript);
            EventBusManager.publish(new AppLogEvent("STT Sanitized: [" + sanitized + "]"));

            boolean streamingMode = systemSession.isStreamingModeOn();
            if (streamingMode) {
                String voiceName = systemSession.getAIVoice().getName();
                if (sanitized.toLowerCase().contains("computer")
                    || sanitized.toLowerCase().contains(voiceName.toLowerCase())) {
                    sendToAi(sanitized);
                }
            } else {
                sendToAi(sanitized);
            }

        } catch (Exception e) {
            log.error("Whisper transcription failed: {}", e.getMessage(), e);
        }
    }

    private void sendToAi(String transcript) {
        EventBusManager.publish(new TTSInterruptEvent());
        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1);
        log.info("Dispatching transcript: {}", transcript);
        EventBusManager.publish(new UserInputEvent(transcript, 1.0f)); // Whisper has no confidence score
    }

    @Subscribe
    public void onIsSpeakingEvent(IsSpeakingEvent event) {
        isSpeaking.set(event.isSpeaking());
    }

    private float[] pcm16ToFloat(byte[] pcm) {
        float[] samples = new float[pcm.length / 2];
        for (int i = 0; i < samples.length; i++) {
            short s = (short) ((pcm[i * 2 + 1] << 8) | (pcm[i * 2] & 0xFF));
            samples[i] = s / 32768.0f;
        }
        return samples;
    }

    private double calculateRMS(byte[] buffer, int length) {
        if (length < 2) return 0.0;
        double sum = 0.0;
        int samples = length / 2;
        for (int i = 0; i < length; i += 2) {
            int val = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
            if (val > 32767) val -= 65536;
            sum += (double) val * val;
        }
        return Math.sqrt(sum / samples);
    }

    private void retryWithBackoff(int retryCount) {
        long backoff = Math.min(BASE_BACKOFF_MS * (long) Math.pow(2, retryCount), MAX_BACKOFF_MS);
        log.info("Retrying after {}ms (attempt {})", backoff, retryCount + 1);
        try {
            Thread.sleep(backoff);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
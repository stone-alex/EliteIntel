package elite.intel.ai.ears.parakeet;

import com.google.common.eventbus.Subscribe;
import com.k2fsa.sherpa.onnx.*;
import elite.intel.ai.ears.*;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.TTSInterruptEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AppPaths;
import elite.intel.util.AudioPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static elite.intel.ai.brain.AIConstants.blockWords;
import static elite.intel.ai.brain.AIConstants.passThroughWords;
import static elite.intel.gameapi.AudioMonitorBus.publish;
import static java.util.Arrays.copyOf;

public class ParakeetSTTImpl implements EarsInterface {

    private static final Logger log = LogManager.getLogger(ParakeetSTTImpl.class);
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int ENTER_VOICE_FRAMES = 1;
    private static final int EXIT_SILENCE_FRAMES = 8;
    private static final int PRE_ROLL_FRAMES = 4;
    private static final long BASE_BACKOFF_MS = 2000;
    private static final long MAX_BACKOFF_MS = 60000;
    private static final long INFERENCE_TIMEOUT_SEC = 5;
    private static final int MIN_AUDIO_MS = 1500;
    private static final int MIN_AUDIO_BYTES = SAMPLE_RATE * 2 * MIN_AUDIO_MS / 1000;
    private static final int MAX_UTTERANCE_MS = 8000;
    private static final int MAX_UTTERANCE_BYTES = SAMPLE_RATE * 2 * MAX_UTTERANCE_MS / 1000;

    private final AtomicBoolean isStopping = new AtomicBoolean(false);
    private final AtomicBoolean isListening = new AtomicBoolean(false);
    private final AtomicBoolean isSpeaking = new AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicInteger pendingTranscriptions = new java.util.concurrent.atomic.AtomicInteger(0);
    private final SystemSession systemSession = SystemSession.getInstance();
    private final ByteArrayOutputStream audioCollector = new ByteArrayOutputStream();
    private final ArrayDeque<byte[]> preRoll = new ArrayDeque<>();

    private ExecutorService transcriptionExecutor;
    private OfflineRecognizer recognizer;
    private Resampler resampler;
    private int sampleRateHertz;
    private int bufferSize;
    public double RMS_THRESHOLD_HIGH;
    public double NOISE_FLOOR;
    private final double MINIMUM_NOISE_FLOOR_TO_RMS_RATIO = 300;
    private Thread processingThread;

    public ParakeetSTTImpl() {
        EventBusManager.register(this);
    }

    @Override
    public void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("Parakeet STT already running");
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
            this.NOISE_FLOOR = cal.getRmsLow();
        } else {
            this.RMS_THRESHOLD_HIGH = high;
            this.NOISE_FLOOR = low;
        }

        recognizer = buildRecognizer();
        log.info("Parakeet recognizer loaded from {}", AppPaths.getParakeetModelDir());

        transcriptionExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Parakeet-Transcription");
            t.setDaemon(true);
            return t;
        });
        isListening.set(true);
        processingThread = new Thread(this::captureLoop);
        processingThread.start();

        if (RMS_THRESHOLD_HIGH == 0 || NOISE_FLOOR == 0) {
            EventBusManager.publish(new AiVoxResponseEvent("Audio calibration required"));
        } else if (RMS_THRESHOLD_HIGH < 250 || RMS_THRESHOLD_HIGH - NOISE_FLOOR < MINIMUM_NOISE_FLOOR_TO_RMS_RATIO) {
            EventBusManager.publish(new AiVoxResponseEvent("Voice input enabled. WARNING: Insufficient noise floor to input signal ratio. The communication may be compromised. Please adjust microphone settings."));
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Voice input enabled"));
        }
    }

    @Override
    public void stop() {
        isStopping.set(true);
        isListening.set(false);
        if (processingThread != null) processingThread.interrupt();

        if (transcriptionExecutor != null) {
            transcriptionExecutor.shutdown();
            try {
                transcriptionExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (recognizer != null) {
            recognizer.release();
            recognizer = null;
        }
        isStopping.set(false);
        EventBusManager.publish(new AiVoxResponseEvent("Voice input disabled."));
    }

    private OfflineRecognizer buildRecognizer() {
        Path modelDir = AppPaths.getParakeetModelDir();
        Path encoderFile = modelDir.resolve("encoder.int8.onnx");
        Path decoderFile = modelDir.resolve("decoder.int8.onnx");
        Path joinerFile = modelDir.resolve("joiner.int8.onnx");
        Path tokensFile = modelDir.resolve("tokens.txt");

        if (!Files.exists(encoderFile)) throw new IllegalStateException("Parakeet encoder missing at: " + encoderFile);
        if (!Files.exists(decoderFile)) throw new IllegalStateException("Parakeet decoder missing at: " + decoderFile);
        if (!Files.exists(joinerFile)) throw new IllegalStateException("Parakeet joiner missing at: " + joinerFile);
        if (!Files.exists(tokensFile)) throw new IllegalStateException("Parakeet tokens missing at: " + tokensFile);

        OfflineTransducerModelConfig transducer = OfflineTransducerModelConfig.builder()
                .setEncoder(encoderFile.toString())
                .setDecoder(decoderFile.toString())
                .setJoiner(joinerFile.toString())
                .build();

        OfflineModelConfig modelConfig = OfflineModelConfig.builder()
                .setTransducer(transducer)
                .setTokens(tokensFile.toString())
                .setNumThreads(Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), systemSession.getSttThreads())))
                .setDebug(false)
                .setProvider("cpu")
                .build();

        FeatureConfig featureConfig = FeatureConfig.builder()
                .setSampleRate(SAMPLE_RATE)
                .setFeatureDim(128)
                .build();

        OfflineRecognizerConfig.Builder configBuilder = OfflineRecognizerConfig.builder()
                .setFeatureConfig(featureConfig)
                .setOfflineModelConfig(modelConfig);

        Path hotwordsFile = modelDir.resolve("hotwords.txt");
        if (Files.exists(hotwordsFile)) {
            try {
                log.info("Encoding hotwords from {} using tokens {}", hotwordsFile, tokensFile);
                HotwordEncoder encoder = new HotwordEncoder(tokensFile);
                log.info("HotwordEncoder ready - sample: 'transfer' → '{}'", encoder.encode("transfer"));
                Path encodedHotwords = encoder.encodeFile(hotwordsFile);
                log.info("Hotwords temp file: {}", encodedHotwords);
                List<String> sampleLines = Files.readAllLines(encodedHotwords).stream().limit(5).toList();
                log.info("Encoded hotwords sample (first 5): {}", sampleLines);
                configBuilder.setHotwordsFile(encodedHotwords.toString());
                configBuilder.setHotwordsScore(1.0f);
                configBuilder.setDecodingMethod("modified_beam_search");
                configBuilder.setBlankPenalty(1.0f);
            } catch (Exception e) {
                log.warn("Failed to encode hotwords, running without hotword boosting: {} {}", e.getClass().getSimpleName(), e.getMessage());
            }
        }

        return new OfflineRecognizer(configBuilder.build());
    }

    private void captureLoop() {
        if (sampleRateHertz != SAMPLE_RATE) {
            resampler = new Resampler(sampleRateHertz, SAMPLE_RATE, CHANNELS);
            log.info("Resampling {} → {} Hz", sampleRateHertz, SAMPLE_RATE);
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
            preRoll.clear();

            while (isListening.get() && line.isOpen()) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead <= 0) continue;

                byte[] audio = (resampler != null)
                        ? resampler.resample(buffer, bytesRead)
                        : buffer;
                int audioLen = (resampler != null) ? audio.length : bytesRead;

                double rms = calculateRMS(audio, audioLen);

                publish(new AudioMonitorEvent(
                        copyOf(audio, audioLen), audioLen, rms, NOISE_FLOOR, RMS_THRESHOLD_HIGH));

                preRoll.addLast(copyOf(audio, audioLen));
                if (preRoll.size() > PRE_ROLL_FRAMES) preRoll.removeFirst();

                if (rms > RMS_THRESHOLD_HIGH) {
                    consecutiveVoice++;
                    consecutiveSilence = 0;
                    audio = Amplifier.amplify(audio);
                } else {
                    consecutiveVoice = 0;
                    consecutiveSilence++;
                }

                if (!isActive && consecutiveVoice >= ENTER_VOICE_FRAMES && !isSpeaking.get()) {
                    isActive = true;
                    audioCollector.reset();
                    for (byte[] frame : preRoll) audioCollector.write(frame, 0, frame.length);
                    preRoll.clear();
                    log.info("VAD: speech started (rms={}, threshold={})", (int) rms, (int) RMS_THRESHOLD_HIGH);
                }

                boolean wasActive = isActive;
                if (isActive && consecutiveSilence >= EXIT_SILENCE_FRAMES) {
                    isActive = false;
                    log.debug("VAD: speech ended");
                }
                if (isActive) {
                    audioCollector.write(audio, 0, audioLen);
                    if (audioCollector.size() >= MAX_UTTERANCE_BYTES) {
                        isActive = false;
                        consecutiveSilence = 0;
                        log.warn("VAD: max utterance length ({}ms) reached, forcing gate close", MAX_UTTERANCE_MS);
                    }
                }
                if (wasActive && !isActive && audioCollector.size() > 0) {
                    final byte[] utterance = audioCollector.toByteArray();
                    DumpAudioForTesting.getInstance().dumpAudioAsWav(utterance, SAMPLE_RATE);
                    audioCollector.reset();
                    int pending = pendingTranscriptions.get();
                    if (pending > 0) log.warn("Transcription queue backed up: {} utterances waiting", pending);
                    pendingTranscriptions.incrementAndGet();
                    submitWithTimeout(utterance);
                }
            }
        }
    }

    private void submitWithTimeout(byte[] utterance) {
        Future<?> future = transcriptionExecutor.submit(() -> transcribeAndDispatch(utterance));
        Thread watchdog = new Thread(() -> {
            try {
                future.get(INFERENCE_TIMEOUT_SEC, TimeUnit.SECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                future.cancel(true);
                log.error("Parakeet inference hung after {}s - replacing executor", INFERENCE_TIMEOUT_SEC);
                transcriptionExecutor.shutdownNow();
                transcriptionExecutor = Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "Parakeet-Transcription");
                    t.setDaemon(true);
                    return t;
                });
            } catch (Exception e) {
                // task completed with exception - already logged in transcribeAndDispatch
            }
        }, "Parakeet-Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private void transcribeAndDispatch(byte[] pcmBytes) {
        pendingTranscriptions.decrementAndGet();
        try {
            float[] samples = pcm16ToFloat(padAudio(pcmBytes));

            long timeStart = System.currentTimeMillis();
            OfflineStream stream = recognizer.createStream();
            try {
                stream.acceptWaveform(samples, SAMPLE_RATE);
                recognizer.decode(stream);
                OfflineRecognizerResult result = recognizer.getResult(stream);
                String transcript = result.getText().toLowerCase().trim();
                log.debug("Parakeet transcription took {} ms", System.currentTimeMillis() - timeStart);

                if (transcript.isBlank() || transcript.length() < 3) return;

                // EventBusManager.publish(new AppLogDebugEvent("RAW: [" + transcript + "]"));
                ///String sanitized = STTSanitizer.getInstance().correctMistakes(transcript);
                //String sanitized = transcript;
                if (blockWord(transcript)) return;

                EventBusManager.publish(new AppLogEvent("STT: [" + transcript + "]"));

                if (systemSession.isStreamingModeOn()) {
                    if (passThrough(transcript)) sendToAi(transcript);
                } else {
                    sendToAi(transcript);
                }
            } finally {
                stream.release();
            }
        } catch (Exception e) {
            log.error("Parakeet transcription failed: {}", e.getMessage(), e);
        }
    }


    private boolean blockWord(String transctipt) {
        for (String word : blockWords) {
            /// if the transcript is block word and nothing else. - ignore
            if (word.equalsIgnoreCase(transctipt)) return true;

            /// if the transcript contains block word. - remove it and continue
            if (transctipt.startsWith(word)) {
                transctipt.replace(word, "");
                return false;
            }
            transctipt.replace("of", "off");
            transctipt.replace("manax", "max");
        }
        return false;
    }

    private boolean passThrough(String transcript) {
        for (String word : passThroughWords) {
            if (transcript.toLowerCase().contains(word)) return true;
        }
        return false;
    }

    private void sendToAi(String transcript) {
        EventBusManager.publish(new TTSInterruptEvent());
        AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_1);
        log.info("Dispatching transcript: {}", transcript.replace("computer", ""));
        EventBusManager.publish(new UserInputEvent(transcript.replace("computer", ""), 1.0f));
    }

    @Subscribe
    public void onIsSpeakingEvent(IsSpeakingEvent event) {
        isSpeaking.set(event.isSpeaking());
    }

    private byte[] padAudio(byte[] pcm) {
        if (pcm.length >= MIN_AUDIO_BYTES) return pcm;
        byte[] padded = new byte[MIN_AUDIO_BYTES];
        System.arraycopy(pcm, 0, padded, 0, pcm.length);
        return padded;
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

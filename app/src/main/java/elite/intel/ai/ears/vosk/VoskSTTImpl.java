package elite.intel.ai.ears.vosk;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.TtsEvent;
import elite.intel.ai.ears.AudioCalibrator;
import elite.intel.ai.ears.AudioFormatDetector;
import elite.intel.ai.ears.AudioSettingsTuple;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.STTSanitizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoskSTTImpl implements EarsInterface {
    private static final Logger log = LogManager.getLogger(VoskSTTImpl.class);

    private int sampleRateHertz;  // Dynamically detected
    private int bufferSize; // Dynamically calculated based on sample rate
    private double RMS_THRESHOLD_HIGH; // Dynamically calibrated
    private double RMS_THRESHOLD_LOW; // Dynamically calibrated

    private static final int CHANNELS = 1; // Mono
    private static final int STREAM_DURATION_MS = 290000; // ~4 min 50 sec
    private static final int KEEP_ALIVE_INTERVAL_MS = 250; // For sending audio chunks
    private static final int ENTER_VOICE_FRAMES = 1; // Quick enter to avoid clipping
    private static final int EXIT_SILENCE_FRAMES = 10; // ~1s silence to exit
    private static final long BASE_BACKOFF_MS = 2000; // Base backoff for retries
    private static final long MAX_BACKOFF_MS = 60000; // Cap at 1 min
    private static final long MIN_STREAM_GAP_MS = 30000; // Enforce 30s between stream starts

    private Model voskModel;
    private final AtomicBoolean isListening = new AtomicBoolean(true);
    private final AtomicBoolean isSpeaking = new AtomicBoolean(false);
    private long lastAudioSentTime = System.currentTimeMillis();
    private Thread processingThread;
    private Map<String, String> corrections;

    public VoskSTTImpl() {
        EventBusManager.register(this);
        // Set Vosk logging level
        LibVosk.setLogLevel(LogLevel.DEBUG);
    }

    @Override
    public void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("Speech recognition is already running");
            return;
        }
        corrections = STTSanitizer.getInstance().getCorrections();
        isListening.set(true);

        // Detect audio format
        AudioSettingsTuple<Integer, Integer> formatResult = AudioFormatDetector.detectSupportedFormat();
        this.sampleRateHertz = formatResult.getSampleRate();
        this.bufferSize = formatResult.getBufferSize();

        SystemSession systemSession = SystemSession.getInstance();
        Double rms_threshold_high = systemSession.getRmsThresholdHigh();
        Double rms_threshold_low = systemSession.getRmsThresholdLow();

        if (rms_threshold_high == null || rms_threshold_low == null) {
            EventBusManager.publish(new AppLogEvent("Calibrating audio..."));
            AudioSettingsTuple<Double, Double> rmsThresholds = AudioCalibrator.calibrateRMS(sampleRateHertz, bufferSize);
            this.RMS_THRESHOLD_HIGH = rmsThresholds.getSampleRate();
            this.RMS_THRESHOLD_LOW = rmsThresholds.getBufferSize();
        } else {
            this.RMS_THRESHOLD_HIGH = rms_threshold_high;
            this.RMS_THRESHOLD_LOW = rms_threshold_low;
            log.info("RMS thresholds already calibrated: High: {}, Low: {}", this.RMS_THRESHOLD_HIGH, this.RMS_THRESHOLD_LOW);
        }

        // Initialize Vosk model
        try {
            String modelPath = extractModelToTempDir();
            this.voskModel = new Model(modelPath);
            log.info("Vosk model initialized successfully at {}", modelPath);
        } catch (Exception e) {
            log.error("Failed to initialize Vosk model: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Vosk model", e);
        }

        // Start processing thread
        this.processingThread = new Thread(this::startStreaming);
        this.processingThread.start();
        if (SystemSession.getInstance().getRmsThresholdLow() != null) {
            EventBusManager.publish(new AiVoxResponseEvent("Voice Input Enabled"));
        }
    }

    @Override
    public void stop() {
        isListening.set(false);
        if (voskModel != null) {
            voskModel.close();
            log.info("Vosk model closed");
        }
        if (processingThread != null) {
            this.processingThread.interrupt();
        }
        EventBusManager.publish(new AiVoxResponseEvent("Voice input disabled."));
    }

    private void startStreaming() {
        int retryCount = 0;
        StringBuffer currentTranscript = new StringBuffer();
        List<Float> confidences = Collections.synchronizedList(new ArrayList<>());
        long lastStreamStart = 0;

        while (isListening.get()) {
            // Enforce minimum gap between stream starts
            long timeSinceLastStream = System.currentTimeMillis() - lastStreamStart;
            if (timeSinceLastStream < MIN_STREAM_GAP_MS) {
                long delay = MIN_STREAM_GAP_MS - timeSinceLastStream;
                log.info("Enforcing stream gap, waiting {}ms", delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    log.warn("Stream gap wait interrupted");
                    break;
                }
            }

            log.info("Starting new streaming session... (Time since last start: {}ms)", System.currentTimeMillis() - lastStreamStart);
            lastStreamStart = System.currentTimeMillis();
            AtomicBoolean needRestart = new AtomicBoolean(false);

            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);

                // Initialize recognizer
                try (Recognizer recognizer = new Recognizer(voskModel, sampleRateHertz)) {
                    AudioFormat format = new AudioFormat(sampleRateHertz, 16, CHANNELS, true, false);
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                    try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
                        log.info("Using streaming format: SampleRate={}, Channels={}", sampleRateHertz, CHANNELS);
                        line.open(format, bufferSize);
                        line.start();
                        byte[] buffer = new byte[bufferSize];
                        byte[] silentBuffer = new byte[bufferSize]; // Zero-filled buffer for keep-alive

                        // Reset VAD state per session
                        boolean isActive = false;
                        int consecutiveVoice = 0;
                        int consecutiveSilence = 0;

                        long lastLoopTime = System.currentTimeMillis();
                        while (isListening.get() && !needRestart.get() && line.isOpen() && (System.currentTimeMillis() - lastStreamStart) < STREAM_DURATION_MS) {
                            int bytesRead = line.read(buffer, 0, buffer.length);
                            long loopDuration = System.currentTimeMillis() - lastLoopTime;
                            log.debug("Bytes read: {}, Loop duration: {}ms, VAD active: {}", bytesRead, loopDuration, isActive);
                            lastLoopTime = System.currentTimeMillis();

                            byte[] trimmedBuffer = silentBuffer; // Default to silent buffer
                            if (bytesRead > 0) {
                                trimmedBuffer = new byte[bytesRead];
                                System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
                            } else {
                                log.warn("No audio data read: bytesRead={}", bytesRead);
                            }

                            // Compute RMS for VAD
                            double rms = bytesRead > 0 ? calculateRMS(trimmedBuffer, bytesRead) : 0.0;

                            // Update VAD state with hysteresis
                            if (rms > RMS_THRESHOLD_HIGH) {
                                consecutiveVoice++;
                                consecutiveSilence = 0;
                            } else {
                                consecutiveVoice = 0;
                                if (rms < RMS_THRESHOLD_LOW) {
                                    consecutiveSilence++;
                                } else {
                                    consecutiveSilence = 0;
                                }
                            }

                            if (!isActive && consecutiveVoice >= ENTER_VOICE_FRAMES && !isSpeaking.get()) {
                                isActive = true;
                                log.debug("VAD: Entered active state (voice detected)");
                            }
                            boolean wasActive = isActive;
                            if (isActive && consecutiveSilence >= EXIT_SILENCE_FRAMES) {
                                isActive = false;
                                log.debug("VAD: Exited active state (silence detected)");
                            }

                            long currentTime = System.currentTimeMillis();
                            boolean sendKeepAlive = (currentTime - lastAudioSentTime) >= KEEP_ALIVE_INTERVAL_MS;

                            if (isActive || sendKeepAlive) {
                                ByteBuffer wrap = ByteBuffer.wrap(trimmedBuffer);
                                if (recognizer.acceptWaveForm(wrap.array(), wrap.array().length)) {
                                    // Process final result
                                    String resultJson = recognizer.getResult();
                                    processVoskResult(resultJson, currentTranscript, confidences);
                                } else {
                                    // Process partial result
                                    String partialJson = recognizer.getPartialResult();
                                    processVoskPartialResult(partialJson, currentTranscript);
                                }
                                lastAudioSentTime = currentTime;
                                log.debug("Processed audio: keepAlive={}, RMS={}, size={}", sendKeepAlive && !isActive, rms, trimmedBuffer.length);
                            } else {
                                log.debug("Skipping send (silence, RMS: {})", rms);
                            }

                            // Check for VAD exit and send accumulated transcript if any
                            if (wasActive && !isActive && currentTranscript.length() > 0) {
                                processFinalTranscript(currentTranscript, confidences);
                            }
                        }

                        // Process any remaining transcript on stream close
                        if (currentTranscript.length() > 0) {
                            processFinalTranscript(currentTranscript, confidences);
                        }
                    }
                }
            } catch (LineUnavailableException | IllegalArgumentException e) {
                log.error("Audio capture failed: {}", e.getMessage(), e);
                needRestart.set(true);
            } catch (Exception e) {
                log.error("Streaming recognition failed: {}", e.getMessage(), e);
                needRestart.set(true);
            }

            if (needRestart.get()) {
                long backoff = Math.min(BASE_BACKOFF_MS * (long) Math.pow(2, retryCount), MAX_BACKOFF_MS);
                log.info("Stream failed, retrying after {}ms (attempt {})", backoff, retryCount + 1);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    log.warn("Retry interrupted");
                    break;
                }
                retryCount++;
            } else {
                retryCount = 0; // Reset on success
            }
        }
    }

    private String extractModelToTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("vosk-model");
        Path modelDir = tempDir.resolve("vosk-en-us");

        // List all model files (adjust based on your model's structure)
        String[] modelFiles = {
                "am/final.mdl", "conf/model.conf", "ivector/final.ie",
        };
        for (String file : modelFiles) {
            Path target = modelDir.resolve(file);
            Files.createDirectories(target.getParent());
            try (var inputStream = VoskSTTImpl.class.getResourceAsStream("/model/vosk-en-us/" + file)) {
                if (inputStream != null) {
                    Files.copy(inputStream, target);
                } else {
                    throw new IOException("Model file not found: " + file);
                }
            }
        }
        return modelDir.toString();
    }

    private void loadNativeLibrary() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String libName;
        if (os.contains("win")) {
            libName = "libvosk.dll";
        } else if (os.contains("linux")) {
            libName = "libvosk.so";
        } else if (os.contains("mac")) {
            libName = "libvosk.dylib";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        Path tempDir = Files.createTempDirectory("vosk-native");
        Path libPath = tempDir.resolve(libName);
        try (var inputStream = VoskSTTImpl.class.getResourceAsStream("/native/" + libName)) {
            if (inputStream != null) {
                Files.copy(inputStream, libPath);
                System.load(libPath.toString());
            } else {
                throw new IOException("Native library not found: " + libName);
            }
        }
    }

    private double calculateRMS(byte[] buffer, int length) {
        if (length < 2) return 0.0;
        double sum = 0.0;
        int samples = length / 2; // 16-bit samples
        for (int i = 0; i < length; i += 2) {
            int val = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
            if (val > 32767) val -= 65536; // Convert to signed
            sum += (double) val * val;
        }
        return Math.sqrt(sum / samples);
    }

    private void processVoskResult(String resultJson, StringBuffer currentTranscript, List<Float> confidences) {
        // Parse Vosk JSON (e.g., {"text": "hello world"})
        try {
            String transcript = extractTextFromJson(resultJson);
            if (!transcript.isEmpty()) {
                currentTranscript.append(transcript).append(" ");
                synchronized (confidences) {
                    confidences.add(0.85f); // Vosk doesn't provide confidence; use default
                }
                log.debug("Processed final Vosk result: {}", transcript);
            }
        } catch (Exception e) {
            log.error("Failed to parse Vosk result JSON: {}", resultJson, e);
        }
    }

    private void processVoskPartialResult(String partialJson, StringBuffer currentTranscript) {
        // Parse Vosk partial JSON (e.g., {"partial": "hello"})
        try {
            String partial = extractTextFromJson(partialJson);
            if (!partial.isEmpty()) {
                log.debug("Processed partial Vosk result: {}", partial);
                // Optionally update UI or log partial results
                EventBusManager.publish(new AppLogEvent("STT Partial: [" + partial + "]"));
            }
        } catch (Exception e) {
            log.error("Failed to parse Vosk partial JSON: {}", partialJson, e);
        }
    }

    private String extractTextFromJson(String json) {
        // Simple JSON parsing for Vosk output
        String key = json.contains("\"partial\"") ? "partial" : "text";
        int start = json.indexOf(key + "\":\"") + key.length() + 3;
        int end = json.lastIndexOf("\"");
        if (start >= 0 && end > start) {
            return json.substring(start, end).trim();
        }
        return "";
    }

    private void processFinalTranscript(StringBuffer currentTranscript, List<Float> confidences) {
        String fullTranscript = currentTranscript.toString().trim();
        EventBusManager.publish(new AppLogEvent("STT Heard: [" + fullTranscript + "]."));
        if (!fullTranscript.isBlank() && fullTranscript.length() >= 3) {
            log.info("Final accumulated transcript: {}", fullTranscript);
            String sanitizedTranscript = STTSanitizer.getInstance().correctMistakes(fullTranscript);
            EventBusManager.publish(new AppLogEvent("STT Sanitized: [" + sanitizedTranscript + "]."));
            boolean isStreamingModeOn = SystemSession.getInstance().isStreamingModeOn();
            float avgConfidence;
            synchronized (confidences) {
                avgConfidence = (float) confidences.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
            }
            if (avgConfidence > 0.3) {
                if (isStreamingModeOn) {
                    String voiceName = SystemSession.getInstance().getAIVoice().getName();
                    if (sanitizedTranscript.toLowerCase().startsWith("computer") || sanitizedTranscript.toLowerCase().contains(voiceName.toLowerCase())) {
                        sendToAi(sanitizedTranscript.replace("computer,", "").replace(voiceName.toLowerCase() + ",", ""), avgConfidence);
                    }
                } else {
                    sendToAi(sanitizedTranscript, avgConfidence);
                }
            } else {
                log.info("Discarded transcript: {} (avg confidence: {})", fullTranscript, avgConfidence);
            }
        }
        currentTranscript.setLength(0);
        synchronized (confidences) {
            confidences.clear();
        }
    }

    private void sendToAi(String sanitizedTranscript, float confidence) {
        AudioPlayer.getInstance().playBeep();
        log.info("Processing sanitizedTranscript: {}", sanitizedTranscript);
        EventBusManager.publish(new UserInputEvent(sanitizedTranscript, confidence));
    }

    @Subscribe
    public void onTtsEvent(TtsEvent event) {
        isSpeaking.set(event.isSpeaking());
    }
}
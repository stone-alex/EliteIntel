package elite.intel.ai.ears;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calibrates audio input levels for the VAD gate.
 * <p>
 * Phase 1 – Noise floor: captures ambient audio and uses the 75th-percentile
 * RMS to represent the typical ambient level robustly.  A hardcoded spike
 * threshold would miss the noise floor entirely in loud environments (e.g. music
 * at ~1400 RMS – every sample exceeds 50 and all are discarded, so the floor
 * would be reported as ~0).
 * <p>
 * Phase 2 – Speech: captures speech and computes the average RMS of frames that
 * exceed twice the noise floor.
 * <p>
 * The VAD trigger threshold (highThreshold / getRmsHigh) is set at the midpoint
 * between the noise floor and the average speech RMS, which is always above
 * ambient noise and below speech regardless of absolute levels.
 */
public class AudioCalibrator {
    private static final Logger log = LogManager.getLogger(AudioCalibrator.class);
    private static final int NOISE_CALIBRATION_DURATION_MS = 5000;
    private static final int SPEECH_CALIBRATION_DURATION_MS = 5000;
    private static final int TTS_PROMPT_DELAY_MS = 3000;
    private static final double DEFAULT_RMS_THRESHOLD_HIGH = 0;
    private static final double DEFAULT_RMS_THRESHOLD_LOW = 0;
    // Percentile used to estimate the noise floor from the collected samples.
    // 75th percentile captures typical ambient level while ignoring the top 25%
    // (transient peaks, brief louder music passages, etc.).
    private static final double NOISE_PERCENTILE = 0.75;
    // Fraction of the noise-to-speech gap used to set the VAD trigger.
    // 0.5 = midpoint: triggers halfway between ambient and average speech.
    private static final double GATE_MIDPOINT_FACTOR = 0.5;
    private static final double MIN_SPEECH_NOISE_GAP = 150.0;
    private static final double MAX_NOISE_AVG = 800.0;


    public static RmsTupple<Double, Double> calibrateRMS(int sampleRateHertz, int bufferSize) {
        log.info("Starting RMS calibration: noise for {}ms, speech for {}ms, with {}ms TTS delays",
                NOISE_CALIBRATION_DURATION_MS, SPEECH_CALIBRATION_DURATION_MS, TTS_PROMPT_DELAY_MS);

        AudioFormat format = new AudioFormat(sampleRateHertz, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        byte[] buffer = new byte[bufferSize];

        // Phase 1: noise floor
        EventBusManager.publish(new AiVoxResponseEvent("Re-calibrating Audio. Remain silent for audio calibration..."));
        log.info("Prompted for noise calibration, waiting {}ms for TTS", TTS_PROMPT_DELAY_MS);
        try {
            Thread.sleep(TTS_PROMPT_DELAY_MS);
        } catch (InterruptedException e) {
            log.warn("Noise TTS delay interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        double noiseFloor = calibrateNoiseFloor(format, bufferSize, buffer, info);

        // Phase 2: speech
        EventBusManager.publish(new AiVoxResponseEvent("Now count to 12 to calibrate audio..."));
        log.info("Prompted for speech calibration, waiting {}ms for TTS", TTS_PROMPT_DELAY_MS);
        try {
            Thread.sleep(TTS_PROMPT_DELAY_MS);
        } catch (InterruptedException e) {
            log.warn("Speech TTS delay interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        double avgSpeechRMS = calibrateSpeech(format, bufferSize, buffer, info, noiseFloor);

        // VAD trigger = midpoint between noise and speech.
        // Always above ambient, always below speech, regardless of absolute levels.
        double gap = avgSpeechRMS - noiseFloor;
        double highThreshold;
        if (avgSpeechRMS <= noiseFloor || gap < MIN_SPEECH_NOISE_GAP) {
            log.warn("Insufficient speech/noise separation (gap={}). Speech may not have been detected or environment is too loud.", gap);
            EventBusManager.publish(new AppLogEvent("WARNING: Low speech/noise gap (" + (int) gap + "). Try speaking louder or reducing ambient noise."));
            // Fallback: 30% above noise floor
            highThreshold = noiseFloor * 1.3 + 50;
        } else {
            highThreshold = noiseFloor + gap * GATE_MIDPOINT_FACTOR;
        }

        // noiseFloor is stored as-is (raw measured ambient level)
        double lowThreshold = noiseFloor;

        highThreshold = Math.round(highThreshold * 100.0) / 100.0;
        lowThreshold = Math.round(lowThreshold * 100.0) / 100.0;

        SystemSession systemSession = SystemSession.getInstance();
        systemSession.setRmsThresholdHigh(highThreshold);
        systemSession.setRmsThresholdLow(lowThreshold);

        log.info("Final calibrated RMS thresholds: HIGH={}, LOW={} (noise floor={}, speech avg={}, gap={})",
                highThreshold, lowThreshold, (int) noiseFloor, (int) avgSpeechRMS, (int) gap);
        EventBusManager.publish(new AppLogEvent("Audio calibration complete: TRIGGER=" + highThreshold + ", NOISE=" + lowThreshold));
        return new RmsTupple<>(highThreshold, lowThreshold);
    }

    private static double calibrateNoiseFloor(AudioFormat format, int bufferSize, byte[] buffer, DataLine.Info info) {
        List<Double> samples = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            line.open(format, bufferSize);
            line.start();
            while (System.currentTimeMillis() - startTime < NOISE_CALIBRATION_DURATION_MS) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    samples.add(calculateRMS(buffer, bytesRead));
                }
            }
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.error("Noise calibration failed: {}", e.getMessage());
            EventBusManager.publish(new AiVoxResponseEvent("Audio calibration failed. Using default settings."));
            EventBusManager.publish(new AppLogEvent("Noise calibration failed: " + e.getMessage()));
            return DEFAULT_RMS_THRESHOLD_LOW;
        }

        if (samples.isEmpty()) {
            log.warn("No noise samples collected");
            return DEFAULT_RMS_THRESHOLD_LOW;
        }

        Collections.sort(samples);
        // 75th percentile: robust estimate of typical ambient level.
        // Works correctly in both quiet environments (returns ~30) and loud
        // environments like music (returns ~1400) because it uses actual
        // measured values rather than filtering against a hardcoded ceiling.
        int idx = Math.min((int) (samples.size() * NOISE_PERCENTILE), samples.size() - 1);
        double noiseFloor = samples.get(idx);

        log.info("Noise calibration: {} samples, min={}, median={}, 75th%={}, max={}",
                samples.size(),
                (int) samples.getFirst().doubleValue(),
                (int) samples.get(samples.size() / 2).doubleValue(),
                (int) noiseFloor,
                (int) samples.getLast().doubleValue());

        if (noiseFloor > MAX_NOISE_AVG) {
            log.warn("High noise floor detected ({}); consider quieter environment", (int) noiseFloor);
            EventBusManager.publish(new AiVoxResponseEvent("Noisy environment detected; calibration may be suboptimal."));
            EventBusManager.publish(new AppLogEvent("High noise floor: " + (int) noiseFloor + " RMS"));
        }
        return noiseFloor;
    }

    private static double calibrateSpeech(AudioFormat format, int bufferSize, byte[] buffer, DataLine.Info info, double noiseFloor) {
        double sumSpeechRMS = 0.0;
        double peakSpeechRMS = 0.0;
        int speechSampleCount = 0;
        int totalSampleCount = 0;
        long startTime = System.currentTimeMillis();

        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            line.open(format, bufferSize);
            line.start();
            while (System.currentTimeMillis() - startTime < SPEECH_CALIBRATION_DURATION_MS) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    double rms = calculateRMS(buffer, bytesRead);
                    totalSampleCount++;
                    if (rms > noiseFloor * 1.3) {
                        sumSpeechRMS += rms;
                        speechSampleCount++;
                        if (rms > peakSpeechRMS) peakSpeechRMS = rms;
                    }
                }
            }
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.error("Speech calibration failed: {}", e.getMessage());
            EventBusManager.publish(new AiVoxResponseEvent("Audio calibration failed. Using default settings."));
            EventBusManager.publish(new AppLogEvent("Speech calibration failed: " + e.getMessage()));
            return DEFAULT_RMS_THRESHOLD_HIGH;
        } finally {
            log.info("Speech calibration: {} total samples, {} speech samples, avg={}, peak={}",
                    totalSampleCount, speechSampleCount,
                    speechSampleCount > 0 ? (int) (sumSpeechRMS / speechSampleCount) : 0,
                    (int) peakSpeechRMS);
        }

        if (speechSampleCount < totalSampleCount / 4) {
            log.warn("Insufficient speech detected ({} speech / {} total). Using noise-based fallback.", speechSampleCount, totalSampleCount);
            EventBusManager.publish(new AppLogEvent("Insufficient speech during calibration."));
            return DEFAULT_RMS_THRESHOLD_HIGH;
        }

        return sumSpeechRMS / speechSampleCount;
    }

    private static double calculateRMS(byte[] buffer, int length) {
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
}

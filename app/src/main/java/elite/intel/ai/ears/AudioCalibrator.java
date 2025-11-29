package elite.intel.ai.ears;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;

/**
 * The AudioCalibrator class is responsible for calibrating audio input levels
 * to determine thresholds for noise floor and speech detection. This calibration
 * ensures proper differentiation between background noise and speech during
 * audio processing.
 * <p>
 * The class uses a two-phase approach:
 * 1. Noise Floor Calibration - Captures silent audio input to determine the
 * background noise level and calculate a suitable lower threshold.
 * 2. Speech Calibration - Captures speech audio input to determine the
 * average and peak speech levels and calculate a suitable upper threshold.
 * <p>
 * Thresholds are dynamically adjusted based on the environment and input
 * conditions to ensure accurate audio analysis. The results of the calibration
 * are saved in a system session for use in subsequent audio processing tasks.
 * <p>
 * Logging is used to provide detailed information about the calibration process,
 * including logging warnings for high noise environments or calibration failures.
 * The thresholds are rounded for consistency and clamped within acceptable ranges.
 * <p>
 * Supporting components include an event bus for communication (e.g., when
 * prompting the user for calibration activities), and the thresholds are updated
 * into the session configuration upon completion.
 * <p>
 * Static constants for calibration timings, noise/speech factors, and threshold
 * clamp values define the calibration behavior. The calibration runs are timed
 * according to the defined durations, filtering input spikes and using fallback
 * defaults as needed during failures.
 * <p>
 * Two private calibration methods perform the noise and speech calibration,
 * utilizing RMS (Root Mean Square) calculations of the audio buffer data.
 */
public class AudioCalibrator {
    private static final Logger log = LogManager.getLogger(AudioCalibrator.class);
    private static final int NOISE_CALIBRATION_DURATION_MS = 5000; // 5 seconds for noise floor
    private static final int SPEECH_CALIBRATION_DURATION_MS = 5000; // 5 seconds for speech
    private static final int TTS_PROMPT_DELAY_MS = 3000; // 3 seconds for TTS vocalization
    private static final double DEFAULT_RMS_THRESHOLD_HIGH = 450; // Fallback
    private static final double DEFAULT_RMS_THRESHOLD_LOW = 90; // Fallback for budget mics
    private static final double NOISE_LOW_FACTOR = 2.5; // Multiplier for low threshold
    private static final double NOISE_LOW_OFFSET = 25.0; // For budget mics
    private static final double SPEECH_HIGH_FACTOR = 0.5; // Set high to half speech avg
    private static final double MIN_HIGH_LOW_RATIO = 3.0; // Ensure high >= low * this
    private static final double MAX_NOISE_AVG = 200.0; // Warn if noise floor too high
    private static final double NOISE_OUTLIER_THRESHOLD = 50.0; // Ignore noise spikes


    /**
     * Calibrates the Root Mean Square (RMS) thresholds for audio input by analyzing the noise floor
     * and user speech levels. This method calculates both a low RMS threshold based on ambient noise
     * and a high RMS threshold based on speech signals. The results are stored in the system session
     * and returned as a tuple.
     *
     * @param sampleRateHertz The sample rate of the audio input in Hertz.
     * @param bufferSize      The size of the audio buffer to be used for recording and analysis.
     * @return A {@code RmsTupple<Double, Double>} representing the calibrated high and low RMS thresholds.
     */
    public static RmsTupple<Double, Double> calibrateRMS(int sampleRateHertz, int bufferSize) {
        log.info("Starting RMS calibration: noise for {}ms, speech for {}ms, with {}ms TTS delays",
                NOISE_CALIBRATION_DURATION_MS, SPEECH_CALIBRATION_DURATION_MS, TTS_PROMPT_DELAY_MS
        );

        AudioFormat format = new AudioFormat(sampleRateHertz, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        byte[] buffer = new byte[bufferSize];

        // Step 1: Calibrate noise floor (remain silent)
        EventBusManager.publish(new AiVoxResponseEvent("Re-calibrating Audio. Remain silent for audio calibration..."));
        log.info("Prompted for noise calibration, waiting {}ms for TTS", TTS_PROMPT_DELAY_MS);
        try {
            Thread.sleep(TTS_PROMPT_DELAY_MS); // Wait for TTS to vocalize
        } catch (InterruptedException e) {
            log.warn("Noise TTS delay interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        double avgNoiseRMS = calibrateNoiseFloor(format, bufferSize, buffer, info);

        double lowThreshold = Math.max(DEFAULT_RMS_THRESHOLD_LOW, avgNoiseRMS * NOISE_LOW_FACTOR + NOISE_LOW_OFFSET);
        log.info("Calibrated low threshold from noise: {}", lowThreshold);

        // Step 2: Calibrate speech
        EventBusManager.publish(new AiVoxResponseEvent("Now count to 12 to calibrate audio..."));
        log.info("Prompted for speech calibration, waiting {}ms for TTS", TTS_PROMPT_DELAY_MS);
        try {
            Thread.sleep(TTS_PROMPT_DELAY_MS); // Wait for TTS to vocalize
        } catch (InterruptedException e) {
            log.warn("Speech TTS delay interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        double highThreshold = calibrateSpeech(format, bufferSize, buffer, info, lowThreshold);

        // Ensure reasonable ratio, clamp
        if (highThreshold < lowThreshold * MIN_HIGH_LOW_RATIO) {
            log.warn("Speech high too close to low; adjusting to fallback ratio");
            highThreshold = lowThreshold * MIN_HIGH_LOW_RATIO;
        }
        highThreshold = Math.min(highThreshold, 2000.0); // Reasonable clamp for speech
        lowThreshold = Math.max(avgNoiseRMS + 15.0, lowThreshold); // Dynamic min based on noise

        // Round to 2 decimal places
        highThreshold = Math.round(highThreshold * 100.0) / 100.0;
        lowThreshold = Math.round(lowThreshold * 100.0) / 100.0;

        SystemSession systemSession = SystemSession.getInstance();
        systemSession.setRmsThresholdHigh(highThreshold);
        systemSession.setRmsThresholdLow(lowThreshold + 10);

        log.info("Final calibrated RMS thresholds: HIGH={}, LOW={}", highThreshold, lowThreshold);
        EventBusManager.publish(new AppLogEvent("Audio calibration complete: HIGH=" + highThreshold + ", LOW=" + lowThreshold));
        return new RmsTupple<>(highThreshold, lowThreshold);
    }

    private static double calibrateNoiseFloor(AudioFormat format, int bufferSize, byte[] buffer, DataLine.Info info) {
        double sumRMS = 0.0;
        double peakRMS = 0.0;
        int sampleCount = 0;
        int validSampleCount = 0;
        long startTime = System.currentTimeMillis();

        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            line.open(format, bufferSize);
            line.start();
            while (System.currentTimeMillis() - startTime < NOISE_CALIBRATION_DURATION_MS) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    double rms = calculateRMS(buffer, bytesRead);
                    sampleCount++;
                    if (rms < NOISE_OUTLIER_THRESHOLD) { // Filter out spikes
                        sumRMS += rms;
                        validSampleCount++;
                    }
                    if (rms > peakRMS) peakRMS = rms;
                    log.trace("Noise calibration RMS sample: {} (valid: {})", rms, rms < NOISE_OUTLIER_THRESHOLD);
                }
            }
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.error("Noise calibration failed: {}", e.getMessage());
            EventBusManager.publish(new AiVoxResponseEvent("Audio calibration failed. Using default settings."));
            EventBusManager.publish(new AppLogEvent("Noise calibration failed: " + e.getMessage()));
            return DEFAULT_RMS_THRESHOLD_LOW / NOISE_LOW_FACTOR; // Fallback avg noise estimate
        } finally {
            log.info("Noise calibration completed. Total samples: {}, Valid samples: {}, Average RMS: {}, Peak RMS: {}",
                    sampleCount, validSampleCount, validSampleCount > 0 ? sumRMS / validSampleCount : 0.0, peakRMS);
        }

        double avgNoise = validSampleCount > 0 ? sumRMS / validSampleCount : 0.0;
        if (avgNoise > MAX_NOISE_AVG) {
            log.warn("High noise floor detected (avg: {}); consider quieter environment", avgNoise);
            EventBusManager.publish(new AiVoxResponseEvent("Noisy environment detected; calibration may be suboptimal."));
            EventBusManager.publish(new AppLogEvent("High noise floor detected: avg RMS=" + avgNoise));
        }
        return avgNoise;
    }

    private static double calibrateSpeech(AudioFormat format, int bufferSize, byte[] buffer, DataLine.Info info, double lowThreshold) {
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
                    if (rms > lowThreshold * 2) { // Qualify as speech (above low with margin)
                        sumSpeechRMS += rms;
                        speechSampleCount++;
                        if (rms > peakSpeechRMS) peakSpeechRMS = rms;
                        log.trace("Speech calibration RMS sample: {}", rms);
                    }
                }
            }
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.error("Speech calibration failed: {}", e.getMessage());
            EventBusManager.publish(new AiVoxResponseEvent("Audio calibration failed. Using default settings."));
            EventBusManager.publish(new AppLogEvent("Speech calibration failed: " + e.getMessage()));
            return DEFAULT_RMS_THRESHOLD_HIGH;
        } finally {
            log.info("Speech calibration completed. Total samples: {}, Speech samples: {}, Average speech RMS: {}, Peak speech RMS: {}",
                    totalSampleCount, speechSampleCount, speechSampleCount > 0 ? sumSpeechRMS / speechSampleCount : 0.0, peakSpeechRMS);
        }

        if (speechSampleCount < totalSampleCount / 4) {
            log.warn("Insufficient speech detected (speech samples: {}). Using default high.", speechSampleCount);
            EventBusManager.publish(new AppLogEvent("Insufficient speech detected during calibration. Using default high threshold."));
            return DEFAULT_RMS_THRESHOLD_HIGH;
        }

        double avgSpeechRMS = sumSpeechRMS / speechSampleCount;
        return avgSpeechRMS * SPEECH_HIGH_FACTOR;
    }

    /**
     * Calculates the Root Mean Square (RMS) value of an audio buffer.
     * Reuses logic from GoogleSTTImpl for consistency.
     *
     * @param buffer The audio buffer in 16-bit little-endian format.
     * @param length The number of bytes to process.
     * @return The RMS value of the audio signal.
     */
    private static double calculateRMS(byte[] buffer, int length) {
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
}
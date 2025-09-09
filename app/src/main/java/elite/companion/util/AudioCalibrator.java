package elite.companion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;

/**
 * Utility class for calibrating audio RMS thresholds for voice activity detection.
 * Analyzes a short audio sample to determine dynamic RMS thresholds for voice and silence detection.
 * Ensures compatibility with varying audio hardware and environments.
 */
public class AudioCalibrator {
    private static final Logger log = LoggerFactory.getLogger(AudioCalibrator.class);
    private static final int CALIBRATION_DURATION_MS = 3000; // 3 seconds
    private static final double DEFAULT_RMS_THRESHOLD_HIGH = 250.0; // Fallback
    private static final double DEFAULT_RMS_THRESHOLD_LOW = 20.0; // Fallback
    private static final double HIGH_THRESHOLD_MULTIPLIER = 1.5; // Scale for voice detection
    private static final double LOW_THRESHOLD_MULTIPLIER = 0.2; // Scale for silence detection

    /**
     * Calibrates RMS thresholds based on a short audio sample from the provided audio line.
     *
     * @param sampleRateHertz The sample rate of the audio format.
     * @param bufferSize      The buffer size for audio capture.
     * @return AudioSettingsTuple containing RMS_THRESHOLD_HIGH and RMS_THRESHOLD_LOW.
     */
    public static AudioSettingsTuple<Double, Double> calibrateRMS(int sampleRateHertz, int bufferSize) {
        log.info("Starting RMS calibration for {}ms", CALIBRATION_DURATION_MS);
        AudioFormat format = new AudioFormat(sampleRateHertz, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        byte[] buffer = new byte[bufferSize];
        double sumRMS = 0.0;
        int sampleCount = 0;
        long startTime = System.currentTimeMillis();

        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            line.open(format, bufferSize);
            line.start();
            while (System.currentTimeMillis() - startTime < CALIBRATION_DURATION_MS) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    double rms = calculateRMS(buffer, bytesRead);
                    sumRMS += rms;
                    sampleCount++;
                    log.trace("Calibration RMS sample: {}", rms);
                }
            }
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.error("Calibration failed: {}", e.getMessage());
            return new AudioSettingsTuple<>(DEFAULT_RMS_THRESHOLD_HIGH, DEFAULT_RMS_THRESHOLD_LOW);
        } finally {
            log.info("Calibration completed. Samples: {}, Average RMS: {}", sampleCount, sampleCount > 0 ? sumRMS / sampleCount : 0.0);
        }

        if (sampleCount == 0) {
            log.warn("No audio samples captured during calibration. Using default thresholds.");
            return new AudioSettingsTuple<>(DEFAULT_RMS_THRESHOLD_HIGH, DEFAULT_RMS_THRESHOLD_LOW);
        }

        double avgRMS = sumRMS / sampleCount;
        double highThreshold = avgRMS * HIGH_THRESHOLD_MULTIPLIER;
        double lowThreshold = avgRMS * LOW_THRESHOLD_MULTIPLIER;

        // Ensure thresholds are within reasonable bounds
        highThreshold = Math.max(50.0, Math.min(highThreshold, 1500.0));
        lowThreshold = Math.max(10.0, Math.min(lowThreshold, 100.0));

        log.info("Calibrated RMS thresholds: HIGH={}, LOW={}", highThreshold, lowThreshold);
        return new AudioSettingsTuple<>(highThreshold, lowThreshold);
    }

    /**
     * Calculates the Root Mean Square (RMS) value of an audio buffer.
     * Reuses logic from GoogleSTTImpl to ensure consistency.
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
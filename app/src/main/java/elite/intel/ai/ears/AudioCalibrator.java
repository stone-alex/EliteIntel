package elite.intel.ai.ears;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.session.SystemSession;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import javax.sound.sampled.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The AudioCalibrator class provides functionality to calibrate the Root Mean Square (RMS)
 * thresholds for audio input based on real-time audio analysis. This process is designed
 * to dynamically adjust audio sensitivity for improved performance.
 */
public class AudioCalibrator {
    private static final Logger log = LogManager.getLogger(AudioCalibrator.class);
    private static final int CALIBRATION_DURATION_MS = 10000; // 10 seconds for calibration
    private static final int TTS_TIMEOUT_MS = 3000; // 3 seconds max wait for TTS
    private static final double DEFAULT_RMS_THRESHOLD_HIGH = 150.0; // Fallback
    private static final double DEFAULT_RMS_THRESHOLD_LOW = 15.0; // Fallback
    private static final double MIN_SPEECH_RMS = 100.0; // Minimum RMS for speech

    /**
     * Calibrates RMS thresholds based on a short audio sample from the provided audio line.
     * Waits for TTS prompt to complete before starting calibration.
     *
     * @param sampleRateHertz The sample rate of the audio format.
     * @param bufferSize      The buffer size for audio capture.
     * @return AudioSettingsTuple containing RMS_THRESHOLD_HIGH and RMS_THRESHOLD_LOW.
     */
    public static AudioSettingsTuple<Double, Double> calibrateRMS(int sampleRateHertz, int bufferSize) {
        log.info("Starting RMS calibration for {}ms", CALIBRATION_DURATION_MS);

        CompletableFuture<Void> ttsPlaybackStarted = new CompletableFuture<>();
        Object ttsSubscriber = new Object() {
            @Subscribe
            public void onTTSPlaybackStartEvent(TTSPlaybackStartEvent event) {
                if (event.getText().equals("Please speak for a few seconds to calibrate audio...")) {
                    log.debug("TTS playback start event received");
                    ttsPlaybackStarted.complete(null);
                }
            }
        };
        EventBusManager.register(ttsSubscriber);

        EventBusManager.publish(new VocalisationRequestEvent("Please speak for a few seconds to calibrate audio..."));

        try {
            ttsPlaybackStarted.get(TTS_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            log.info("TTS playback started");
        } catch (TimeoutException e) {
            log.warn("TTS playback timed out after {}ms", TTS_TIMEOUT_MS);
        } catch (Exception e) {
            log.error("TTS playback wait failed: {}", e.getMessage());
        } finally {
            EventBusManager.unregister(ttsSubscriber);
        }

        AudioFormat format = new AudioFormat(sampleRateHertz, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        byte[] buffer = new byte[bufferSize];
        double sumRMS = 0.0;
        double peakRMS = 0.0;
        int sampleCount = 0;
        int speechSamples = 0;
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
                    if (rms > peakRMS) peakRMS = rms;
                    if (rms > MIN_SPEECH_RMS) speechSamples++;
                    log.trace("Calibration RMS sample: {}", rms);
                }
            }
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.error("Calibration failed: {}", e.getMessage());
            EventBusManager.publish(new VocalisationRequestEvent("Audio calibration failed. Using default settings."));
            return new AudioSettingsTuple<>(DEFAULT_RMS_THRESHOLD_HIGH, DEFAULT_RMS_THRESHOLD_LOW);
        } finally {
            log.info("Calibration completed. Samples: {}, Speech samples: {}, Average RMS: {}, Peak RMS: {}",
                    sampleCount, speechSamples, sampleCount > 0 ? sumRMS / sampleCount : 0.0, peakRMS);
            EventBusManager.publish(new VocalisationRequestEvent("Audio calibration complete."));
        }

        if (sampleCount == 0 || speechSamples < sampleCount / 4) {
            log.warn("Insufficient speech detected during calibration (speech samples: {}). Using default thresholds.", speechSamples);
            return new AudioSettingsTuple<>(150.0, 15.0);
        }

        double avgRMS = sumRMS / sampleCount;
        double highThreshold = Math.max(avgRMS * 1.0, peakRMS * 0.2); // Lower multiplier to 1.0
        double lowThreshold = avgRMS * 0.3; // Increase to 0.3

        // Clamp thresholds
        highThreshold = Math.max(400.0, Math.min(highThreshold, 800.0));
        lowThreshold = Math.max(50.0, Math.min(lowThreshold, 80.0));

        SystemSession systemSession = SystemSession.getInstance();
        systemSession.setRmsThresholdHigh(highThreshold);
        systemSession.setRmsThresholdLow(lowThreshold);

        log.info("Calibrated RMS thresholds: HIGH={}, LOW={}", highThreshold, lowThreshold);
        return new AudioSettingsTuple<>(highThreshold, lowThreshold);
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
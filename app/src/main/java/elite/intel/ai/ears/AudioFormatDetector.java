package elite.intel.ai.ears;


import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.Optional;


/**
 * The AudioFormatDetector class is responsible for detecting supported audio formats for mono 16-bit
 * input by evaluating various predefined sample rates. It determines the optimal configuration for
 * audio processing, including sample rate and buffer size, by querying the system's audio capabilities.
 */
public class AudioFormatDetector {

    private static final Logger log = LogManager.getLogger(AudioFormatDetector.class);
    private static final int CHANNELS = 1; // Mono
    private static final int[] POSSIBLE_RATES = {48000, 44100, 16000}; // Preferred rates in order
    private static final double BUFFER_DURATION_SECONDS = 0.1; // 100ms buffer
    private static final int BYTES_PER_SAMPLE = 2; // 16-bit = 2 bytes

    public static Format detectSupportedFormat() {
        AudioFormatDetector detector = new AudioFormatDetector();
        try {
            return detector.detectSupportedFormatInternal().orElseThrow(() -> new AudioFormatException("No supported audio format found for mono 16-bit input."));
        } catch (AudioFormatException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Detects a supported audio format for mono 16-bit input by checking available sample rates.
     *
     * @return Optional containing AudioSettingsTuple with sample rate and buffer size, or empty if none found
     * @throws AudioFormatException if an error occurs during detection (e.g., system issues)
     */
    public Optional<Format> detectSupportedFormatInternal() throws AudioFormatException {
        for (int rate : POSSIBLE_RATES) {
            try {
                AudioFormat format = new AudioFormat(rate, 16, CHANNELS, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if (AudioSystem.isLineSupported(info)) {
                    int bufferSize = calculateBufferSize(rate);
                    if (bufferSize <= 0) {
                        log.warn("Invalid buffer size calculated for rate {}: {}", rate, bufferSize);
                        continue; // Skip invalid rates
                    }
                    log.info("Detected supported sample rate: {} Hz, buffer size: {} bytes", rate, bufferSize);
                    return Optional.of(new Format(rate, bufferSize));
                }
            } catch (Exception e) {
                log.error("Error checking support for sample rate {}: {}", rate, e.getMessage(), e);
                throw new AudioFormatException("Failed to detect audio format due to system error: " + e.getMessage());
            }
        }
        log.warn("No supported audio format found for mono 16-bit input.");
        EventBusManager.publish(new AiVoxResponseEvent("No supported audio format found for mono 16-bit input."));
        return Optional.empty();
    }

    private int calculateBufferSize(int rate) {
        return (int) (rate * BUFFER_DURATION_SECONDS * BYTES_PER_SAMPLE * CHANNELS);
    }

    public record Format(int sampleRate, int bufferSize) {
        public int getSampleRate() {
            return sampleRate;
        }

        public int getBufferSize() {
            return bufferSize;
        }
    }
}
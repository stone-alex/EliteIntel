package elite.intel.ai.ears;


import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.util.Optional;


/**
 * The AudioFormatDetector class is responsible for detecting supported audio formats for mono 16-bit
 * input by evaluating various predefined sample rates. It determines the optimal configuration for
 * audio processing, including sample rate and buffer size, by querying the system's audio capabilities.
 */
public class AudioFormatDetector {

    private static final Logger log = LogManager.getLogger(AudioFormatDetector.class);
    private static final int CHANNELS = 1; // Mono
    private static final int[] POSSIBLE_RATES = {48000, 44100, 96000, 192000}; // Preferred rates in order
    private static final double BUFFER_DURATION_SECONDS = 0.1; // 100ms buffer
    private static final int BYTES_PER_SAMPLE = 2; // 16-bit = 2 bytes

    public static Format detectSupportedFormat() {
        return detectSupportedFormat(null);
    }

    public static Format detectSupportedFormat(Mixer.Info mixerInfo) {
        AudioFormatDetector detector = new AudioFormatDetector();
        try {
            return detector.detectSupportedFormatInternal(mixerInfo).orElseThrow(() -> new AudioFormatException("No supported audio format found for mono 16-bit input."));
        } catch (AudioFormatException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Detects the first supported audio format for mono 16-bit input from a list of predefined sample rates.
     * This method verifies system or mixer support for each possible rate and calculates a buffer size
     * for the supported format.
     *
     * @param mixerInfo the {@link Mixer.Info} instance representing the audio mixer to query for format support.
     *                  If null, the method checks system-wide support.
     * @return an {@link Optional} containing the supported {@link Format} if detected, or an empty {@link Optional}
     *         if no supported format is found.
     * @throws AudioFormatException if a system error occurs while attempting to detect the supported audio format.
     */
    public Optional<Format> detectSupportedFormatInternal(Mixer.Info mixerInfo) throws AudioFormatException {
        for (int rate : POSSIBLE_RATES) {
            try {
                AudioFormat format = new AudioFormat(rate, 16, CHANNELS, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                boolean supported;
                if (mixerInfo == null) {
                    supported = AudioSystem.isLineSupported(info);
                } else {
                    try {
                        supported = AudioSystem.getMixer(mixerInfo).isLineSupported(info);
                    } catch (Exception ex) {
                        log.warn("Could not query mixer '{}': {}", mixerInfo.getName(), ex.getMessage());
                        supported = false;
                    }
                }
                if (supported) {
                    int bufferSize = calculateBufferSize(rate);
                    if (bufferSize <= 0) {
                        log.warn("Invalid buffer size calculated for rate {}: {}", rate, bufferSize);
                        continue;
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
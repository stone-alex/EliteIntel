package elite.companion.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * Utility class for detecting supported audio formats for mono 16-bit input.
 */
public class AudioFormatDetector {
    private static final Logger log = LoggerFactory.getLogger(AudioFormatDetector.class);
    private static final int CHANNELS = 1; // Mono
    private static final int[] POSSIBLE_RATES = {48000, 44100, 16000}; // Preferred rates in order

    /**
     * Detects a supported audio format for mono 16-bit input by checking available sample rates.
     *
     * @return CustomPair containing the detected sample rate (key) and buffer size (value)
     * @throws RuntimeException if no supported format is found
     */
    public static AudioSettingsTuple<Integer, Integer> detectSupportedFormat() {
        for (int rate : POSSIBLE_RATES) {
            AudioFormat format = new AudioFormat(rate, 16, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (AudioSystem.isLineSupported(info)) {
                int bufferSize = (int) (rate * 0.1 * 2 * CHANNELS); // ~100ms buffer
                log.info("Detected supported sample rate: {} Hz, buffer size: {}", rate, bufferSize);
                return new AudioSettingsTuple<>(rate, bufferSize);
            }
        }
        throw new RuntimeException("No supported audio format found for mono 16-bit input");
    }
}
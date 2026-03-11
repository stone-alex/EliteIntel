package elite.intel.ai.ears;

/**
 * Per-frame RMS-based Automatic Gain Control (AGC) for streaming 16-bit LE PCM audio.
 * <p>
 * Unlike the two-pass peak normalizer (Amplifier), this works on individual frames
 * as they arrive from the mic, making it suitable for live streaming to Google STT.
 * <p>
 * Design:
 * - Measures RMS of each frame
 * - Smooths the gain estimate using separate attack/release time constants
 * (fast attack = react quickly to loud bursts; slow release = don't amplify
 * momentary silences between words)
 * - Targets a fixed RMS level (-18 dBFS is a safe mid-level for STT engines)
 * - Hard clips at ±32767 as a safety net
 * - Skips frames that are below a noise floor threshold (silence/noise stays silent)
 */
public class StreamNormalizer {

    // Target RMS level: -18 dBFS  →  32767 * 10^(-18/20) ≈ 3277
    private static final double TARGET_DBFS = -18.0;
    private static final double TARGET_RMS = 32767.0 * Math.pow(10.0, TARGET_DBFS / 20.0);

    // Maximum gain to avoid blowing up quiet noise into loud noise
    private static final double MAX_GAIN = 4.0;
    private static final double MIN_GAIN = 0.1;

    // Attack: how fast gain *decreases* when audio gets loud (fast = protect ears/clipping)
    // Release: how fast gain *increases* when audio gets quiet (slow = don't amplify silence)
    // Values are per-frame smoothing coefficients (closer to 1.0 = slower response)
    private static final double ATTACK_COEFF = 0.40; // ~fast: reacts within a few frames
    private static final double RELEASE_COEFF = 0.98; // ~slow: takes many frames to recover

    // Frames with RMS below this are considered silence — gain is held, not updated
    private static final double SILENCE_RMS_THRESHOLD = 200.0;

    private double smoothedGain = 1.0;

    /**
     * Normalize a single PCM frame in-place (returns a new buffer).
     *
     * @param frame  16-bit little-endian PCM bytes
     * @param length number of valid bytes in frame
     * @return gain-adjusted PCM frame (same length)
     */
    public byte[] normalize(byte[] frame, int length) {
        if (frame == null || length < 2) return frame;

        int len = length & ~1;
        double rms = calculateRMS(frame, len);

        if (rms > SILENCE_RMS_THRESHOLD) {
            // Compute ideal gain for this frame
            double idealGain = TARGET_RMS / rms;
            idealGain = Math.min(idealGain, MAX_GAIN);
            idealGain = Math.max(idealGain, MIN_GAIN);

            // Smooth: use fast attack when gain needs to DROP, slow release when it needs to RISE
            double coeff = (idealGain < smoothedGain) ? ATTACK_COEFF : RELEASE_COEFF;
            smoothedGain = smoothedGain * (1.0 - coeff) + idealGain * coeff;
        }
        // else: silence — hold current gain, don't let it creep up during pauses

        // Apply smoothed gain
        byte[] output = new byte[length];
        for (int i = 0; i < len; i += 2) {
            int sample = (frame[i] & 0xFF) | (frame[i + 1] << 8); // LE sign-extended
            long amplified = Math.round(sample * smoothedGain);
            if (amplified > 32767) amplified = 32767;
            if (amplified < -32768) amplified = -32768;
            output[i] = (byte) (amplified & 0xFF);
            output[i + 1] = (byte) ((amplified >> 8) & 0xFF);
        }
        // preserve any trailing odd byte
        if (len < length) output[len] = frame[len];

        return output;
    }

    /**
     * Reset gain state — call this when starting a new stream session
     * so stale gain from a previous session doesn't bleed in.
     */
    public void reset() {
        smoothedGain = 1.0;
    }

    public double getCurrentGain() {
        return smoothedGain;
    }

    private double calculateRMS(byte[] buffer, int length) {
        if (length < 2) return 0.0;
        double sum = 0.0;
        int samples = length / 2;
        for (int i = 0; i < length; i += 2) {
            int val = (buffer[i] & 0xFF) | (buffer[i + 1] << 8);
            if (val > 32767) val -= 65536;
            sum += (double) val * val;
        }
        return Math.sqrt(sum / samples);
    }
}
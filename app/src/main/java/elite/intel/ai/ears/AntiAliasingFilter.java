package elite.intel.ai.ears;

/**
 * 2nd-order Butterworth IIR low-pass filter (Direct Form II Transposed).
 * <p>
 * Applied to raw PCM before downsampling to suppress aliasing. Without this,
 * linear-interpolation resampling folds energy above the target Nyquist back
 * into the passband, producing harsh sibilant artefacts on s/sh/z sounds.
 * <p>
 * Cutoff is set to 90% of the target Nyquist, e.g. 7,200 Hz when downsampling
 * to 16 kHz, giving a clean transition band before the aliasing zone at 8 kHz.
 * <p>
 * State (w1, w2) is preserved across calls so there are no discontinuities at
 * frame boundaries.
 */
public class AntiAliasingFilter {

    private final double b0, b1, b2, a1, a2;
    private double w1 = 0.0;
    private double w2 = 0.0;

    /**
     * @param sourceSampleRate Hz of the incoming audio (e.g. 48000)
     * @param targetSampleRate Hz of the downsampled output (e.g. 16000)
     */
    public AntiAliasingFilter(int sourceSampleRate, int targetSampleRate) {
        double fc = 0.90 * targetSampleRate / 2.0;                   // e.g. 7200 Hz
        double K = Math.tan(Math.PI * fc / sourceSampleRate);
        double Q = Math.sqrt(2.0) / 2.0;                            // Butterworth Q
        double K2 = K * K;
        double norm = 1.0 + K / Q + K2;

        b0 = K2 / norm;
        b1 = 2.0 * b0;
        b2 = b0;
        a1 = 2.0 * (K2 - 1.0) / norm;
        a2 = (1.0 - K / Q + K2) / norm;
    }

    /**
     * Filters a block of 16-bit little-endian PCM.
     * State carries over to the next call so frame boundaries are seamless.
     *
     * @param input  raw PCM bytes
     * @param length number of bytes to process (must be even)
     * @return filtered PCM bytes (same length as input, rounded down to even)
     */
    public byte[] filter(byte[] input, int length) {
        int len = length & ~1;
        byte[] output = new byte[len];
        for (int i = 0; i < len; i += 2) {
            double x = (short) ((input[i + 1] << 8) | (input[i] & 0xFF));
            double y = b0 * x + w1;
            w1 = b1 * x - a1 * y + w2;
            w2 = b2 * x - a2 * y;
            int s = (int) Math.round(y);
            if (s > 32767) s = 32767;
            if (s < -32768) s = -32768;
            output[i] = (byte) (s & 0xFF);
            output[i + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return output;
    }
}

package elite.intel.ai.ears;

/**
 * The Resampler class provides functionality to resample audio data from one sample rate
 * to another using linear interpolation. It is designed to handle audio data stored as
 * byte arrays in little-endian format (16-bit PCM). The resampling process adjusts the
 * number of samples based on the ratio between the source and target sample rates.
 */
public class Resampler {
    private final double ratio;
    private final byte[] leftover = new byte[4]; // max 2 samples
    private int leftoverBytes = 0;

    public Resampler(int sourceRate, int targetRate, int channels) {
        this.ratio = (double) sourceRate / targetRate;
    }

    public byte[] resample(byte[] input, int length) {
        if (length <= 0) return new byte[0];

        int totalInputBytes = length + leftoverBytes;
        short[] inSamples = new short[totalInputBytes / 2];
        // copy leftover + new data
        System.arraycopy(leftover, 0, inSamples, 0, leftoverBytes / 2);
        for (int i = 0, j = leftoverBytes; i < length; i += 2, j++) {
            inSamples[j] = (short) ((input[i + 1] & 0xff) << 8 | (input[i] & 0xff));
        }

        int outSamples = (int) (totalInputBytes / 2 / ratio) + 1;
        short[] out = new short[outSamples];
        int outIdx = 0;

        double pos = 0;
        while (pos < inSamples.length - 1) {
            int i1 = (int) pos;
            int i2 = i1 + 1;
            double frac = pos - i1;
            short s = (short) (inSamples[i1] + frac * (inSamples[i2] - inSamples[i1]));
            out[outIdx++] = s;
            pos += ratio;
        }

        // save leftover (normally 0–1 sample)
        int usedSamples = (int) pos;
        leftoverBytes = (inSamples.length - usedSamples) * 2;
        if (leftoverBytes > 0) {
            short last = inSamples[inSamples.length - 1];
            leftover[0] = (byte) (last & 0xff);
            leftover[1] = (byte) (last >> 8 & 0xff);
        }

        byte[] result = new byte[outIdx * 2];
        for (int i = 0; i < outIdx; i++) {
            result[i*2]   = (byte) (out[i] & 0xff);
            result[i*2+1] = (byte) (out[i] >> 8);
        }
        return result;
    }
}

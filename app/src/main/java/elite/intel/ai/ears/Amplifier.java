package elite.intel.ai.ears;

public class Amplifier {

    public static byte[] amplify(byte[] audioData, double gain) {
        if (gain == 1.0) return audioData;
        byte[] output = new byte[audioData.length];
        for (int i = 0; i < audioData.length; i += 2) {
            // Reconstruct 16-bit signed sample (Little Endian)
            int sample = (audioData[i + 1] << 8) | (audioData[i] & 0xFF);
            if (sample > 32767) sample -= 65536;

            // Apply gain and clip to prevent overflow/distortion
            int amplified = (int) (sample * gain);
            if (amplified > 32767) amplified = 32767;
            else if (amplified < -32768) amplified = -32768;

            // Write back to bytes
            output[i] = (byte) (amplified & 0xFF);
            output[i + 1] = (byte) ((amplified >> 8) & 0xFF);
        }
        return output;
    }

}

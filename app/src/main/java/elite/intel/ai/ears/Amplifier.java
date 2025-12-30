package elite.intel.ai.ears;

public class Amplifier {

    public static byte[] amplify(byte[] audioData, double gain) {
        if (gain == 1.0 || audioData == null) {
            return audioData;
        }

        int len = audioData.length & ~1; // truncate to even length
        byte[] output = new byte[audioData.length];

        for (int i = 0; i < len; i += 2) {
            // Little-endian 16-bit signed, proper sign extension
            int sample = (audioData[i] & 0xFF) | (audioData[i + 1] << 8); // already sign-extended by <<

            // Apply gain and hard clip
            long amplified = Math.round(sample * gain);
            if (amplified > 32767) amplified = 32767;
            else if (amplified < -32768) amplified = -32768;

            // Write back LE
            output[i]     = (byte) (amplified & 0xFF);
            output[i + 1] = (byte) ((amplified >> 8) & 0xFF);
        }

        // Copy any trailing odd byte unchanged (should never happen)
        if (len < audioData.length) {
            output[len] = audioData[len];
        }

        return output;
    }

}

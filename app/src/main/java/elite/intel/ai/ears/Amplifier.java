package elite.intel.ai.ears;

/**
 * Audio normalizer for 16-bit little-endian PCM.
 * <p>
 * Replaces blind gain amplification with peak normalization to a target
 * dBFS headroom, so transcription input is consistently loud without clipping.
 * <p>
 * Target: -3 dBFS  →  peak target = 32767 * 10^(-3/20) ≈ 23197
 */
public class Amplifier {

    /**
     * Target peak level in linear scale: -3 dBFS
     */
    private static final double TARGET_DBFS = -3.0;
    private static final double TARGET_PEAK = 32767.0 * Math.pow(10.0, TARGET_DBFS / 20.0); // ≈ 23197

    /**
     * Don't amplify if the audio is already within this many units of the target (avoid micro-gains)
     */
    private static final double MIN_PEAK_TO_NORMALIZE = 100.0;

    /**
     * Normalizes PCM audio to -3 dBFS peak.
     * <p>
     * The gain parameter is kept for API compatibility but is ignored —
     * gain is now derived from the actual peak of the audio data.
     *
     * @param audioData 16-bit little-endian PCM bytes
     * @return normalized audio at -3 dBFS peak, or original if too quiet to normalize
     */
    public static byte[] amplify(byte[] audioData) {
        if (audioData == null || audioData.length < 2) return audioData;

        int len = audioData.length & ~1; // ensure even

        // --- Pass 1: find peak sample magnitude ---
        int peak = 0;
        for (int i = 0; i < len; i += 2) {
            int sample = (audioData[i] & 0xFF) | (audioData[i + 1] << 8); // LE, sign-extended
            int abs = Math.abs(sample);
            if (abs > peak) peak = abs;
        }

        // Nothing useful in the buffer — return as-is
        if (peak < MIN_PEAK_TO_NORMALIZE) return audioData;

        // --- Derive exact gain to hit target peak ---
        double normalizeGain = TARGET_PEAK / peak;

        // Already at or above target — no amplification needed (avoid boosting loud audio)
        if (normalizeGain <= 1.0) return audioData;

        // --- Pass 2: apply gain with hard clip safety net ---
        byte[] output = new byte[audioData.length];
        for (int i = 0; i < len; i += 2) {
            int sample = (audioData[i] & 0xFF) | (audioData[i + 1] << 8);
            long amplified = Math.round(sample * normalizeGain);
            if (amplified > 32767)  amplified = 32767;
            else if (amplified < -32768) amplified = -32768;
            output[i]     = (byte) (amplified & 0xFF);
            output[i + 1] = (byte) ((amplified >> 8) & 0xFF);
        }

        // Preserve any trailing odd byte (shouldn't happen with 16-bit PCM)
        if (len < audioData.length) output[len] = audioData[len];

        return output;
    }
}
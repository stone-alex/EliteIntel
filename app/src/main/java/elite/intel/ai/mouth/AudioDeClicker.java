package elite.intel.ai.mouth;

public class AudioDeClicker {

    public static void sanitize(byte[] audioData, int fadeMs) {
        removeClicks(audioData);
        applyFade(audioData, fadeMs, true);
        //no fade out
        //applyFade(audioData, fadeMs, false);
    }

    private static void removeClicks(byte[] audioData) {
        if ((audioData.length & 1) != 0) {
            byte[] even = new byte[audioData.length - 1];
            System.arraycopy(audioData, 0, even, 0, even.length);
        }
    }

    private static void applyFade(byte[] audioData, int fadeMs, boolean isFadeIn) {
        int samplesToFade = (24000 * fadeMs) / 1000;
        int startIndex = isFadeIn ? 0 : Math.max(0, audioData.length / 2 - samplesToFade);
        for (int i = startIndex; i < startIndex + samplesToFade && (i * 2 + 1) < audioData.length; i++) {
            int lo = audioData[2 * i] & 0xFF;
            int hi = audioData[2 * i + 1] & 0xFF;
            short sample = (short) ((hi << 8) | lo);
            float gain = isFadeIn ? (float) i / samplesToFade : (float) (startIndex + samplesToFade - i) / samplesToFade;
            int scaled = Math.round(sample * gain);
            audioData[2 * i] = (byte) (scaled & 0xFF);
            audioData[2 * i + 1] = (byte) ((scaled >>> 8) & 0xFF);
        }
    }
}

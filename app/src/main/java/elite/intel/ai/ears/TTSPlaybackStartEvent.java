package elite.intel.ai.ears;

/**
 * Represents an event triggered when Text-to-Speech (TTS) playback starts.
 * This event contains information about the text being played.
 * Used in conjunction with AudioCalibrator only..
 */
public class TTSPlaybackStartEvent {
    private final String text;

    public TTSPlaybackStartEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
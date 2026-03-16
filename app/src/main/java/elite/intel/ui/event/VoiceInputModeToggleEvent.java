package elite.intel.ui.event;

public class VoiceInputModeToggleEvent {

    private boolean isStreaming;

    public VoiceInputModeToggleEvent(boolean isStreaming) {
        this.isStreaming = isStreaming;
    }

    public boolean isStreaming() {
        return isStreaming;
    }
}

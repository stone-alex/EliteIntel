package elite.intel.ui.event;

public class ToggleStreamingModeEvent {

    private boolean isStreaming;

    public ToggleStreamingModeEvent(boolean isStreaming) {
        this.isStreaming = isStreaming;
    }

    public boolean isStreaming() {
        return isStreaming;
    }
}

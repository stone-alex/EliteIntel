package elite.intel.ui.event;

public class StreamModelToggleEvent {

    private boolean isStreaming;

    public StreamModelToggleEvent(boolean isStreaming) {
        this.isStreaming = isStreaming;
    }

    public boolean isStreaming() {
        return isStreaming;
    }
}

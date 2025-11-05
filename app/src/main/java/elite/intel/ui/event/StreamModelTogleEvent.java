package elite.intel.ui.event;

public class StreamModelTogleEvent {

    private boolean isStreaming;

    public StreamModelTogleEvent(boolean isStreaming) {
        this.isStreaming = isStreaming;
    }

    public boolean isStreaming() {
        return isStreaming;
    }
}

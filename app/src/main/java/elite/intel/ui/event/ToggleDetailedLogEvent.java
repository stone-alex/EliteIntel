package elite.intel.ui.event;

public class ToggleDetailedLogEvent {
    private boolean isDetailed;

    public ToggleDetailedLogEvent(boolean isDetailed) {
        this.isDetailed = isDetailed;
    }

    public boolean isDetailed() {
        return isDetailed;
    }
}

package elite.intel.ui.event;

public class ToggleSendExplorationDataEvent {

    private boolean isEnabled;

    public ToggleSendExplorationDataEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}

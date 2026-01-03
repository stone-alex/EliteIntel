package elite.intel.ui.event;

public class ToggleSendOutfittingDataEvent {
    private boolean isEnabled;
    public ToggleSendOutfittingDataEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    public boolean isEnabled() {
        return isEnabled;
    }
}

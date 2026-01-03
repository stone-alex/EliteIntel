package elite.intel.ui.event;

public class ToggleSendShipyardDataEvent {
    private boolean isEnabled;
    public ToggleSendShipyardDataEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    public boolean isEnabled() {
        return isEnabled;
    }
}

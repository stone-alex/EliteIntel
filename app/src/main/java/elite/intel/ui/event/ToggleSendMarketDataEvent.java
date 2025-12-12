package elite.intel.ui.event;

public class ToggleSendMarketDataEvent {
    private boolean isEnabled;

    public ToggleSendMarketDataEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}

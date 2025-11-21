package elite.intel.ui.event;


public class TogglePrivacyModeEvent {
    private boolean isEnabled;

    public TogglePrivacyModeEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}

package elite.intel.ui.event;

public class ToggleWakeWordEvent {

    private boolean isOn;

    public ToggleWakeWordEvent(boolean isSleepingMode) {
        this.isOn = isSleepingMode;
    }

    public boolean isOn() {
        return isOn;
    }
}

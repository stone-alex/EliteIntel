package elite.intel.ui.event;

public class ToggleWakeWordEvent {

    private boolean isOn;

    public ToggleWakeWordEvent(boolean isStreaming) {
        this.isOn = isStreaming;
    }

    public boolean isOn() {
        return isOn;
    }
}

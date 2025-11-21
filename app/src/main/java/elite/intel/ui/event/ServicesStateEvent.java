package elite.intel.ui.event;

public class ServicesStateEvent {

    private boolean isRunning;

    public ServicesStateEvent(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isRunning() {
        return isRunning;
    }
}

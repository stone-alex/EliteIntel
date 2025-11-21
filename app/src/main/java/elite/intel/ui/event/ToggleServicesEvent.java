package elite.intel.ui.event;

public class ToggleServicesEvent {

    private boolean startSercice;

    public ToggleServicesEvent(boolean startSercice) {
        this.startSercice = startSercice;
    }

    public boolean isStartSercice() {
        return startSercice;
    }

}

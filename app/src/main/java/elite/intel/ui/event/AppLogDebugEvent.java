package elite.intel.ui.event;

public class AppLogDebugEvent {

    private String data;

    public AppLogDebugEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}

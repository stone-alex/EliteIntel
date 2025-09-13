package elite.intel.ui.event;

public class AppLogEvent {

    private String data;

    public AppLogEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}

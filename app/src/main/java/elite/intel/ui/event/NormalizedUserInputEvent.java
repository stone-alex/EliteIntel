package elite.intel.ui.event;

public class NormalizedUserInputEvent {
    private String text;

    public NormalizedUserInputEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

package elite.companion.ui.event;

public class UiEvent {

    private String command;
    private String action;

    public UiEvent(String command, String action) {
        this.command = command;
        this.action = action;
    }

    public UiEvent(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public String getAction() {
        return action;
    }

}

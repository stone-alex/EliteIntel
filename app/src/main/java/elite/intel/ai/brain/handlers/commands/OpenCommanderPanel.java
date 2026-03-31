package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.CenterPanel;
import elite.intel.session.ui.UINavigator;

public class OpenCommanderPanel implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip() || status.isInSrv() || status.isInFighter()) {
            navigator.openAndNavigate(StatusFlags.GuiFocus.ROLE_PANEL, CenterPanel.COMMANDER);
        }
    }
}

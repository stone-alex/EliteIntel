package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.CenterPanel;
import elite.intel.session.ui.UINavigator;

public class OpenCrewPanelHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip() || status.isInSrv() || status.isInFighter()) {
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0));
            navigator.openAndNavigate(StatusFlags.GuiFocus.ROLE_PANEL, CenterPanel.CREW);
        }
    }
}

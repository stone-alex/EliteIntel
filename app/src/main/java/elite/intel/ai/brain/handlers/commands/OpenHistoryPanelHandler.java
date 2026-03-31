package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.CommsPanel;
import elite.intel.session.ui.UINavigator;

public class OpenHistoryPanelHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip() || status.isInFighter()) {
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0));
            navigator.openAndNavigate(StatusFlags.GuiFocus.COMMS_PANEL, CommsPanel.HISTORY);
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Sorry, I can't do that right now."));
        }
    }
}

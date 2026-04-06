package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

public class ClosePanelHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        if (status.isSystemMapOpen() || status.isGalaxyMapOpen()) {
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0));
        }
        navigator.closeOpenPanel();
    }
}
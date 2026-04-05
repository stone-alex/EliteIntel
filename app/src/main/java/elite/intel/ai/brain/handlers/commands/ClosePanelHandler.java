package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EXIT_KEY;

public class ClosePanelHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        navigator.closeOpenPanel();

        if (status.isSystemMapOpen() || status.isGalaxyMapOpen()) {
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0));
        }

        for (int i = 0; i < 10; i++) { ///  back out of all menus etc
            GameControllerBus.publish(new GameInputEvent(BINDING_EXIT_KEY.getGameBinding(), 0));
        }
    }
}
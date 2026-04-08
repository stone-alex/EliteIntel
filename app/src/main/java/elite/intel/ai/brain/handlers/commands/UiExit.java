package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

public class UiExit {

    public static void close() {
        Status status = Status.getInstance();
        if (status.isSystemMapOpen() || status.isGalaxyMapOpen()) {
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0));
        }
        new UINavigator().closeOpenPanel();
    }
}

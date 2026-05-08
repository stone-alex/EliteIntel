package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.Bindings;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

import static elite.intel.ai.hands.Bindings.GameCommand.*;

public class UiNavCommon {

    public static void close() {
        Status status = Status.getInstance();
        if (status.isSystemMapOpen()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_LOCAL_MAP.getGameBinding(), 0));
        }
        if (status.isGalaxyMapOpen()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_GALAXY_MAP.getGameBinding(), 0));
        }
        new UINavigator().closeOpenPanel();
        GameControllerBus.publish(new GameInputEvent(BINDING_EXIT_KEY.getGameBinding(), 0));
    }


    public static void prepToKnownUiPositionWhileInTheShipAtStation() {
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
    }
}

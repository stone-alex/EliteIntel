package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

import static elite.intel.ai.hands.Bindings.GameCommand.*;

public class OpenLocalMapHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        navigator.closeOpenPanel();
        Status status = Status.getInstance();
        if (status.isInMainShip() || status.isInFighter()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_LOCAL_MAP.getGameBinding(), 0));
        }

        if (status.isInSrv()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_LOCAL_MAP_BUGGY.getGameBinding(), 0));
        }

        if (status.isOnFoot()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_RIGHT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
        }
    }
}

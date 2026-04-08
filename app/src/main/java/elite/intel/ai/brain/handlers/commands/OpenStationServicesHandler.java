package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;

public class OpenStationServicesHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        UiExit.close();

        prepToKnownUiPosition();
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_ACTIVATE.getGameBinding(), 0));
    }


    private void prepToKnownUiPosition() {
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
    }
}

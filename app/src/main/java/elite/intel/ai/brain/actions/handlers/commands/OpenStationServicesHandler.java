package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.Bindings;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;

public class OpenStationServicesHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        UiNavCommon.close();
        UiNavCommon.prepToKnownUiPositionWhileInTheShipAtStation();
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_ACTIVATE.getGameBinding(), 0));
    }
}

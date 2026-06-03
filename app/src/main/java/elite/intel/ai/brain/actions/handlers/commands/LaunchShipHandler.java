package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.Bindings;
import elite.intel.gameapi.GameControllerBus;

public class LaunchShipHandler implements CommandHandler {
    @Override
    public void handle(String action, JsonObject params, String responseText) {
        UiNavCommon.close();
        UiNavCommon.prepToKnownUiPositionWhileInTheShipAtStation();
        GameControllerBus.publish(GameInputSequenceEvent.of(
                GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_UP.getGameBinding()),
                GameInputStep.bindingTap(Bindings.GameCommand.BINDING_ACTIVATE.getGameBinding())
        ));
    }
}

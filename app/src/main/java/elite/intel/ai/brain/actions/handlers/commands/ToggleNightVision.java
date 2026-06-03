package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.gameapi.GameControllerBus;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_NIGHT_VISION_TOGGLE;

public class ToggleNightVision implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_NIGHT_VISION_TOGGLE.getGameBinding())));
    }
}

package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.actions.Commands;
import elite.intel.gameapi.GameControllerBus;


public class SimpleCommandActionHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        if (action != null) {
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(Commands.getGameBinding(action))));
        }
    }

}

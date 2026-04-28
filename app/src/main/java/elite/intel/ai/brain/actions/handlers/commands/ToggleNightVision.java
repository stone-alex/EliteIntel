package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_NIGHT_VISION_TOGGLE;

public class ToggleNightVision implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        GameControllerBus.publish(new GameInputEvent(BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), 0));
    }
}

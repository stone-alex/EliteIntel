package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;


public class SimpleCommandActionHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        if (action != null) {
            GameControllerBus.publish(new GameInputEvent(Commands.getGameBinding(action), 0));
        }
    }

}

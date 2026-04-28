package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_EXIT_SUPERCRUISE;

public class DropFromFtlHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInSupercruise()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_EXIT_SUPERCRUISE.getGameBinding(), 0));
        }
    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE_COMBAT_MODE;

public class ActivateCombatModeHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isAnalysisMode()) {
            if (status.isInMainShip()) {
                GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE_COMBAT_MODE.getGameBinding(), 0));
            }

            if (status.isInSrv()) {
                GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE_COMBAT_MODE.getGameBinding(), 0));
            }
        }
    }
}

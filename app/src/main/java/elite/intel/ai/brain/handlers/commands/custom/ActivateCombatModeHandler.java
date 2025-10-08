package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.ACTIVATE_COMBAT_MODE;

public class ActivateCombatModeHandler extends CustomCommandOperator implements CommandHandler {

    public ActivateCombatModeHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isAnalysisMode()) {
            if (status.isInMainShip()) {
                operateKeyboard(ACTIVATE_COMBAT_MODE.getGameBinding(), 0);
            }

            if (status.isInSrv()) {
                operateKeyboard(ACTIVATE_COMBAT_MODE.getGameBinding(), 0);
            }
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Combat mode already active."));
        }
    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE_COMBAT_MODE;

public class ActivateCombatModeHandler extends CommandOperator implements CommandHandler {

    public ActivateCombatModeHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isAnalysisMode()) {
            if (status.isInMainShip()) {
                operateKeyboard(BINDING_ACTIVATE_COMBAT_MODE.getGameBinding(), 0);
            }

            if (status.isInSrv()) {
                operateKeyboard(BINDING_ACTIVATE_COMBAT_MODE.getGameBinding(), 0);
            }
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Combat mode already active."));
        }
    }
}

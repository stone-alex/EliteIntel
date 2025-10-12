package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE_ANALYSIS_MODE;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE_ANALYSIS_MODE_BUGGY;

public class ActivateAnalysisModeHandler extends CustomCommandOperator implements CommandHandler {

    public ActivateAnalysisModeHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if (!status.isAnalysisMode()) {
            if (status.isInMainShip()) {
                operateKeyboard(BINDING_ACTIVATE_ANALYSIS_MODE.getGameBinding(), 0);
            }

            if (status.isInSrv()) {
                operateKeyboard(BINDING_ACTIVATE_ANALYSIS_MODE_BUGGY.getGameBinding(), 0);
            }
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Analysis mode already active."));
        }
    }
}

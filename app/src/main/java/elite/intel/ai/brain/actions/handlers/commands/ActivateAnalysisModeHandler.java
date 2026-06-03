package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_ACTIVATE_ANALYSIS_MODE;
import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_ACTIVATE_ANALYSIS_MODE_BUGGY;

public class ActivateAnalysisModeHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if (!status.isAnalysisMode()) {
            if (status.isInMainShip()) {
                GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_ACTIVATE_ANALYSIS_MODE.getGameBinding())));
            }

            if (status.isInSrv()) {
                GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_ACTIVATE_ANALYSIS_MODE_BUGGY.getGameBinding())));
            }
        }
    }
}

package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_RESET_POWER_DISTRIBUTION;
import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_RESET_POWER_DISTRIBUTION_BUGGY;

public class ResetPowerSettings implements CommandHandler {



    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            String resetPowerDistribution = BINDING_RESET_POWER_DISTRIBUTION.getGameBinding();
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(resetPowerDistribution)));
        }

        if (status.isInSrv()) {
            String resetPowerDistribution = BINDING_RESET_POWER_DISTRIBUTION_BUGGY.getGameBinding();
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(resetPowerDistribution)));
        }
    }
}

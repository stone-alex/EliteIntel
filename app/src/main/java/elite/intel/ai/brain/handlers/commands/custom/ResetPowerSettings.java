package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.ControllerBindings.GameCommand.BINDING_RESET_POWER_DISTRIBUTION;
import static elite.intel.ai.brain.handlers.commands.ControllerBindings.GameCommand.BINDING_RESET_POWER_DISTRIBUTION_BUGGY;

public class ResetPowerSettings extends CustomCommandOperator implements CommandHandler {


    public ResetPowerSettings(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            String resetPowerDistribution = BINDING_RESET_POWER_DISTRIBUTION.getGameBinding();
            operateKeyboard(resetPowerDistribution, 0);
        }

        if (status.isInSrv()) {
            String resetPowerDistribution = BINDING_RESET_POWER_DISTRIBUTION_BUGGY.getGameBinding();
            operateKeyboard(resetPowerDistribution, 0);
        }
    }
}

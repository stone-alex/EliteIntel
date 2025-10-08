package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.RESET_POWER_DISTRIBUTION;
import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.RESET_POWER_DISTRIBUTION_BUGGY;

public class ResetPowerSettings extends CustomCommandOperator implements CommandHandler {


    public ResetPowerSettings(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
            operateKeyboard(resetPowerDistribution, 0);
        }

        if (status.isInSrv()) {
            String resetPowerDistribution = RESET_POWER_DISTRIBUTION_BUGGY.getGameBinding();
            operateKeyboard(resetPowerDistribution, 0);
        }
    }
}

package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.*;

/**
 * The SetPowerToEnginesHandler class is responsible for handling the "Set Power to Engines"
 * command, which executes a series of keyboard operations to adjust power distribution
 * settings within a game environment. It extends the functionality of the CustomCommandOperator
 * and implements the CommandHandler interface.
 * <p>
 * This handler performs the following sequence:
 * - Resets the power distribution.
 * - Increases power to engines and systems alternately, with delays between each operation.
 * - Logs the status once the operation is complete.
 * <p>
 * Thread safety:
 * - Uses synchronized sleep delays for sequential execution.
 * <p>
 * Error handling:
 * - Catches any interruptions during thread sleep and proceeds without throwing exceptions.
 * <p>
 * Logging:
 * - Provides logging details for executed actions and status updates, ensuring traceability
 * of power adjustment operations.
 * <p>
 * Fields:
 * - DELAY: Represents the delay (in milliseconds) between consecutive keyboard operations.
 * <p>
 * Constructor:
 * - Initializes the handler with a reference to the game command handler.
 * <p>
 * Methods:
 * - handle: Executes the power redistribution operation triggered by the command, using
 * provided parameters and response text.
 */
public class SetPowerToEnginesHandler extends CustomCommandOperator implements CommandHandler {

    private static final Logger log = LogManager.getLogger(SetPowerToEnginesHandler.class);

    public SetPowerToEnginesHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if (status.isInMainShip()) {
            powerToEnginesShip();
        }

        if (status.isInSrv()) {
            powerToEnginesSRV();
        }

    }

    private void powerToEnginesSRV() {
        String resetPowerDistribution = RESET_POWER_DISTRIBUTION_BUGGY.getGameBinding();
        String increaseEnginePower = INCREASE_ENGINES_POWER_BUGGY.getGameBinding();
        String increaseSystemPower = INCREASE_SYSTEMS_POWER_BUGGY.getGameBinding();

        performAction(resetPowerDistribution, increaseEnginePower, increaseSystemPower);

    }

    private void powerToEnginesShip() {
        String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
        String increaseEnginePower = INCREASE_ENGINES_POWER.getGameBinding();
        String increaseSystemPower = INCREASE_SYSTEMS_POWER.getGameBinding();

        performAction(resetPowerDistribution, increaseEnginePower, increaseSystemPower);
    }

    private void performAction(String resetPowerDistribution, String increaseEnginePower, String increaseSystemPower) {
        operateKeyboard(resetPowerDistribution, 0);
        operateKeyboard(increaseEnginePower, 0);
        operateKeyboard(increaseSystemPower, 0);
        operateKeyboard(increaseEnginePower, 0);
        operateKeyboard(increaseSystemPower, 0);
        operateKeyboard(increaseEnginePower, 0);
        log.info("Diverting power to engines");
    }
}

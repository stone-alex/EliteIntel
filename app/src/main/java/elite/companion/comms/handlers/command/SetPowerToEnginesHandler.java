package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.brain.robot.GameCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.comms.handlers.command.CommandActionsGame.GameCommand.*;

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

    private static final Logger log = LoggerFactory.getLogger(SetPowerToEnginesHandler.class);
    public static final int DELAY = 5;

    public SetPowerToEnginesHandler(GameCommandHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

        try {
            String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
            String increaseEnginePower = INCREASE_ENGINES_POWER.getGameBinding();
            String increaseSystemPower = INCREASE_SYSTEMS_POWER.getGameBinding();

            operateKeyboard(resetPowerDistribution, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginePower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginePower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginePower, 0);
            log.info("Diverting power to engines");
        } catch (InterruptedException e) {
            //ok
        }
    }
}

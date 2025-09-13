package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.*;

/**
 * The SetPowerToSystemsHandler class extends the CustomCommandOperator and implements
 * the CommandHandler interface to manage and execute key bindings for redistributing
 * power to a game's systems and engines.
 * <p>
 * This handler coordinates keyboard operations to reset and optimize power distribution
 * for systems and engines by invoking key bindings stored within the game's configuration.
 * The operations are performed with a fixed delay between each action.
 */
public class SetPowerToSystemsHandler extends CustomCommandOperator implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SetPowerToSystemsHandler.class);
    public static final int DELAY = 5;


    public SetPowerToSystemsHandler(GameHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {

            String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
            String increaseSystemsPower = INCREASE_SYSTEMS_POWER.getGameBinding();
            String increaseEnginesPower = INCREASE_ENGINES_POWER.getGameBinding();

            operateKeyboard(resetPowerDistribution, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemsPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginesPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemsPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginesPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseSystemsPower, 0);

            log.info("Power distribution complete");
        } catch (InterruptedException e) {
            //ok
        }
    }
}

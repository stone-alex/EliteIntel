package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger log = LogManager.getLogger(SetPowerToSystemsHandler.class);


    public SetPowerToSystemsHandler(GameHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

        String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
            String increaseSystemsPower = INCREASE_SYSTEMS_POWER.getGameBinding();
            String increaseEnginesPower = INCREASE_ENGINES_POWER.getGameBinding();

            operateKeyboard(resetPowerDistribution, 0);
            operateKeyboard(increaseSystemsPower, 0);
            operateKeyboard(increaseEnginesPower, 0);
            operateKeyboard(increaseSystemsPower, 0);
            operateKeyboard(increaseEnginesPower, 0);
            operateKeyboard(increaseSystemsPower, 0);

            log.info("Power distribution complete");

    }
}

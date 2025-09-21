package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.*;

/**
 * The SetPowerToWeaponsHandler class is responsible for executing a series of predefined
 * game keyboard operations to redistribute power to weapons and engines. It extends the
 * CustomCommandOperator to utilize base keyboard operation functionality and implements
 * the CommandHandler interface to support the handling of game-related commands.
 * <p>
 * This handler resets the power distribution, followed by assigning power to weapons
 * and engines in an alternating manner, maintaining a delay between each operation.
 * The sequence of operations is performed through processing key bindings, which are
 * fetched and executed as defined by the game's commands.
 */
public class SetPowerToWeaponsHandler extends CustomCommandOperator implements CommandHandler {

    private static final Logger log = LogManager.getLogger(SetPowerToWeaponsHandler.class);

    public SetPowerToWeaponsHandler(GameHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

            String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
            String increaseWeaponsPower = INCREASE_WEAPONS_POWER.getGameBinding();
            String increaseEnginesPower = INCREASE_ENGINES_POWER.getGameBinding();

            operateKeyboard(resetPowerDistribution, 0);
            operateKeyboard(increaseWeaponsPower, 0);
            operateKeyboard(increaseEnginesPower, 0);
            operateKeyboard(increaseWeaponsPower, 0);
            operateKeyboard(increaseEnginesPower, 0);
            operateKeyboard(increaseWeaponsPower, 0);

            log.info("Diverting power to weapons");
    }
}

package elite.companion.ai.brain.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.ai.hands.GameCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.ai.brain.handlers.command.CommandActionsGame.GameCommand.*;

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

    private static final Logger log = LoggerFactory.getLogger(SetPowerToWeaponsHandler.class);
    public static final int DELAY = 5;

    public SetPowerToWeaponsHandler(GameCommandHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {

            String resetPowerDistribution = RESET_POWER_DISTRIBUTION.getGameBinding();
            String increaseWeaponsPower = INCREASE_WEAPONS_POWER.getGameBinding();
            String increaseEnginesPower = INCREASE_ENGINES_POWER.getGameBinding();

            operateKeyboard(resetPowerDistribution, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseWeaponsPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginesPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseWeaponsPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseEnginesPower, 0);
            Thread.sleep(DELAY);
            operateKeyboard(increaseWeaponsPower, 0);

            log.info("Diverting power to weapons");
        } catch (InterruptedException e) {
            //ok
        }
    }
}

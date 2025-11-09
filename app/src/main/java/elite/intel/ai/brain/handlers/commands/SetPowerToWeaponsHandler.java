package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

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
public class SetPowerToWeaponsHandler extends CommandOperator implements CommandHandler {

    private static final Logger log = LogManager.getLogger(SetPowerToWeaponsHandler.class);

    public SetPowerToWeaponsHandler(GameController commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            powerToWeaponsShip();
        }

        if (status.isInSrv()) {
            powerToWeaponsSRV();
        }
    }

    private void powerToWeaponsSRV() {
        String resetPowerDistribution = BINDING_RESET_POWER_DISTRIBUTION_BUGGY.getGameBinding();
        String increaseWeaponsPower = BINDING_INCREASE_WEAPONS_POWER_BUGGY.getGameBinding();
        String increaseEnginesPower = BINDING_INCREASE_ENGINES_POWER_BUGGY.getGameBinding();

        performAction(resetPowerDistribution, increaseWeaponsPower, increaseEnginesPower);
    }

    private void powerToWeaponsShip() {
        String resetPowerDistribution = BINDING_RESET_POWER_DISTRIBUTION.getGameBinding();
        String increaseWeaponsPower = BINDING_INCREASE_WEAPONS_POWER.getGameBinding();
        String increaseEnginesPower = BINDING_INCREASE_ENGINES_POWER.getGameBinding();

        performAction(resetPowerDistribution, increaseWeaponsPower, increaseEnginesPower);
    }

    private void performAction(String resetPowerDistribution, String increaseWeaponsPower, String increaseEnginesPower) {
        operateKeyboard(resetPowerDistribution, 0);
        operateKeyboard(increaseWeaponsPower, 0);
        operateKeyboard(increaseWeaponsPower, 0);
        log.info("Diverting power to weapons");
    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

/**
 * The SetPowerToSystemsHandler class extends the CustomCommandOperator and implements
 * the CommandHandler interface to manage and execute key bindings for redistributing
 * power to a game's systems and engines.
 * <p>
 * This handler coordinates keyboard operations to reset and optimize power distribution
 * for systems and engines by invoking key bindings stored within the game's configuration.
 * The operations are performed with a fixed delay between each action.
 */
public class SetPowerToSystemsHandler implements CommandHandler {

    private static final Logger log = LogManager.getLogger(SetPowerToSystemsHandler.class);



    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            powerToSystemsShip();
        }

        if (status.isInSrv()) {
            powerToSystemsSRV();
        }

    }

    private void powerToSystemsSRV() {
        String resetPowerDistribution = BINDING_RESET_POWER_DISTRIBUTION_BUGGY.getGameBinding();
        String increaseSystemsPower = BINDING_INCREASE_SYSTEMS_POWER_BUGGY.getGameBinding();
        String increaseEnginesPower = BINDING_INCREASE_ENGINES_POWER_BUGGY.getGameBinding();

        performOperation(resetPowerDistribution, increaseSystemsPower, increaseEnginesPower);
    }

    private void powerToSystemsShip() {
        String resetPowerDistribution = BINDING_RESET_POWER_DISTRIBUTION.getGameBinding();
        String increaseSystemsPower = BINDING_INCREASE_SYSTEMS_POWER.getGameBinding();
        String increaseEnginesPower = BINDING_INCREASE_ENGINES_POWER.getGameBinding();

        performOperation(resetPowerDistribution, increaseSystemsPower, increaseEnginesPower);
    }

    private void performOperation(String resetPowerDistribution, String increaseSystemsPower, String increaseEnginesPower) {
        GameControllerBus.publish(new GameInputEvent(resetPowerDistribution, 0));
        GameControllerBus.publish(new GameInputEvent(increaseSystemsPower, 0));
        GameControllerBus.publish(new GameInputEvent(increaseSystemsPower, 0));

        log.info("Power distribution complete");
    }
}

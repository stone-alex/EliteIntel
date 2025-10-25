package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_GALAXY_MAP;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_GALAXY_MAP_BUGGY;

/**
 * The OpenGalaxyMapHandler class is responsible for handling commands related to opening
 * and interacting with the galaxy map in a game. It extends CustomCommandOperator to leverage
 * keyboard interaction capabilities and implements the CommandHandler interface to process
 * commands provided to it.
 * <p>
 * When the handler is invoked, it executes a sequence of key bindings to open the galaxy map
 * and perform additional UI navigation actions, ensuring a predefined interaction flow.
 * <p>
 * Key Responsibilities:
 * - Handling commands to open the galaxy map.
 * - Performing related UI navigation operations.
 * <p>
 * Constructor:
 * - The constructor initializes the handler with necessary game monitor and keyboard executor
 * components obtained from the provided GameCommandHandler instance.
 * <p>
 * Methods:
 * - `handle(JsonObject params, String responseText)`: This method processes the input parameters
 * and executes key bindings to achieve the desired operations for interacting with the galaxy map.
 * It ensures appropriate timing between key operations to allow the game UI to respond correctly.
 * <p>
 * Exceptions:
 * - The constructor may throw an Exception if there are issues during its initialization.
 */
public class OpenGalaxyMapHandler extends CommandOperator implements CommandHandler {

    public OpenGalaxyMapHandler(GameController commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        Status status = Status.getInstance();
        if (status.isInMainShip()) {
            operateKeyboard(BINDING_GALAXY_MAP.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(BINDING_GALAXY_MAP_BUGGY.getGameBinding(), 0);
        }
    }
}

package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.hands.GameHandler;

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
public class OpenGalaxyMapHandler extends CustomCommandOperator implements CommandHandler {

    public OpenGalaxyMapHandler(GameHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

        try {
            String openGalaxyMap = GameCommands.GameCommand.GALAXY_MAP.getGameBinding();
            operateKeyboard(openGalaxyMap, 0);
            Thread.sleep(4000);
            String uiLeft = GameCommands.GameCommand.UI_LEFT.getGameBinding();
            operateKeyboard(uiLeft, 0);

            String uiRight = GameCommands.GameCommand.UI_RIGHT.getGameBinding();
            operateKeyboard(uiRight, 0);
        } catch (InterruptedException oops) {
            //ok
        }

    }
}

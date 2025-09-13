package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.hands.GameHandler;

/**
 * OpenSystemMapHandler is responsible for handling the "Open System Map" game command.
 * This class extends CustomCommandOperator to utilize its functionality for
 * executing key bindings associated with the specified game action via the keyboard.
 * Implements the CommandHandler interface to process input parameters and response text
 * for handling the command logic.
 * <p>
 * Constructor:
 * - Initializes the handler by utilizing the given GameCommandHandler to retrieve
 * the necessary BindingsMonitor and KeyBindingExecutor for command execution.
 * <p>
 * Method:
 * - handle(JsonObject params, String responseText): Invokes the keyboard operation
 * for the SYSTEM_MAP command based on the associated key binding.
 */
public class OpenSystemMapHandler extends CustomCommandOperator implements CommandHandler {

    public OpenSystemMapHandler(GameHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        String openMap = GameCommands.GameCommand.SYSTEM_MAP.getGameBinding();
        operateKeyboard(openMap, 0);
    }
}

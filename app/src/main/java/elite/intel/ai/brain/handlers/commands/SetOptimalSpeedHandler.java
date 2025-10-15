package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_SET_SPEED75;

/**
 * The SetOptimalSpeedHandler class is responsible for handling the command
 * to set the optimal speed for the game. It extends the CustomCommandOperator
 * class and implements the CommandHandler interface to override the handle
 * method specific to the logic of setting speed.
 * <p>
 * This handler uses the specified key binding associated with setting speed
 * (SET_SPEED75) and executes the corresponding keyboard operation through
 * the inherited functionality from the CustomCommandOperator class.
 */
public class SetOptimalSpeedHandler extends CustomCommandOperator implements CommandHandler {

    public SetOptimalSpeedHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        String setOptimalSpeed = BINDING_SET_SPEED75.getGameBinding();
        operateKeyboard(setOptimalSpeed, 0);
    }
}

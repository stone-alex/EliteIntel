package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_SET_SPEED75;
import static elite.intel.ai.brain.handlers.commands.Commands.DECREASE_SPEED_BY;

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
public class SetOptimalSpeedHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED75.getGameBinding(), 0)); /// Sets to 75%
        GameControllerBus.publish(new GameInputEvent(DECREASE_SPEED_BY.getBinding(), 0)); /// Decrease by 1 notch
    }
}

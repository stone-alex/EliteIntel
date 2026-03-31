package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

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
public class OpenGalaxyMapHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();

    @Override public void handle(String action, JsonObject params, String responseText) {

        navigator.closeOpenPanel();
        Status status = Status.getInstance();
        if (status.isInMainShip() || status.isInFighter()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_GALAXY_MAP.getGameBinding(), 0));
        }

        if (status.isInSrv()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_GALAXY_MAP_BUGGY.getGameBinding(), 0));
        } if(status.isOnFoot()){
            GameControllerBus.publish(new GameInputEvent(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_RIGHT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
        }
    }
}

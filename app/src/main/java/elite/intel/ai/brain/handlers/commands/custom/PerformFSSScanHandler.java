package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.ai.brain.handlers.commands.ControllerBindings.GameCommand.BINDING_EXPLORATION_FSSDISCOVERY_SCAN;
import static elite.intel.ai.brain.handlers.commands.ControllerBindings.GameCommand.BINDING_SET_SPEED_ZERO;

/**
 * The PerformFSSScanHandler class is responsible for executing a sequence of key bindings
 * to perform a Full Spectrum System (FSS) scan within the game. It extends the
 * CustomCommandOperator class to utilize its keyboard operation capabilities and implements
 * the CommandHandler interface to define the command handling contract.
 * <p>
 * This handler ensures the proper sequence and timing of actions required for the FSS scan,
 * including setting the throttle to zero and triggering the FSS discovery scan key bindings.
 * It makes use of the BindingsMonitor and KeyBindingExecutor provided by the base class for
 * managing and executing the required key bindings.
 * <p>
 * The class also manages error handling during the scan process and logs relevant information
 * to assist in debugging or issue resolution.
 */
public class PerformFSSScanHandler extends CustomCommandOperator implements CommandHandler {

    private static final Logger log = LogManager.getLogger(PerformFSSScanHandler.class);

    public PerformFSSScanHandler(GameController commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
            String actionSetThrottleToZero = BINDING_SET_SPEED_ZERO.getGameBinding();
            String actionPressFSS = BINDING_EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding();

            operateKeyboard(actionSetThrottleToZero, 0);
            operateKeyboard(actionPressFSS, 0);
            operateKeyboard(actionPressFSS, 4500);
    }
}

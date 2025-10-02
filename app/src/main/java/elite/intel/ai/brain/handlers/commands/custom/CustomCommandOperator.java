package elite.intel.ai.brain.handlers.commands.custom;

import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.ai.hands.KeyBindingExecutor;
import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

/**
 * CustomCommandOperator serves as a base class for handling custom keyboard operations
 * tied to game-related commands. It facilitates executing key bindings with a specified
 * hold duration and logs the results of the execution.
 */
public class CustomCommandOperator {
    private static final Logger log = LogManager.getLogger(CustomCommandOperator.class);
    private final BindingsMonitor monitor;
    private final KeyBindingExecutor executor;

    public CustomCommandOperator(BindingsMonitor monitor, KeyBindingExecutor executor) {
        this.monitor = monitor;
        this.executor = executor;
    }


    protected void operateKeyboard(String action, int holdTime) {
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(action);
        if (binding == null) {
            binding = monitor.getBindings().get(GameCommands.getGameBinding(action));
        }

        if (binding != null) {
            executor.executeBindingWithHold(binding, holdTime);
            log.info("Executed action: {} with key: {}", action, binding);
        } else {
            log.warn("No binding found for action: {}", action);
            EventBusManager.publish(new VocalisationRequestEvent("Custom command operator. No key binding found for action " + action));
        }
    }
}

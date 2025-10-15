package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.ai.hands.KeyBindingExecutor;
import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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


    protected void operateKeyboard(String bindingIdentifier, int holdTime) {
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(bindingIdentifier);

        if (binding != null) {
            executor.executeBindingWithHold(binding, holdTime);
        } else {
            log.warn("No binding found for action: {}", bindingIdentifier);
            EventBusManager.publish(new AiVoxResponseEvent("Custom command operator. No key binding found for " + bindingIdentifier));
        }
    }
}

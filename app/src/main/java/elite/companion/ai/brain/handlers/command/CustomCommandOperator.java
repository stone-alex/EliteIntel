package elite.companion.ai.brain.handlers.command;

import elite.companion.ai.hands.BindingsMonitor;
import elite.companion.ai.hands.KeyBindingExecutor;
import elite.companion.ai.hands.KeyBindingsParser;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CustomCommandOperator serves as a base class for handling custom keyboard operations
 * tied to game-related commands. It facilitates executing key bindings with a specified
 * hold duration and logs the results of the execution.
 */
public class CustomCommandOperator {
    private static final Logger log = LoggerFactory.getLogger(CustomCommandOperator.class);
    private final BindingsMonitor monitor;
    private final KeyBindingExecutor executor;

    public CustomCommandOperator(BindingsMonitor monitor, KeyBindingExecutor executor) {
        this.monitor = monitor;
        this.executor = executor;
    }


    protected void operateKeyboard(String action, int holdTime) {
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(action);
        if (binding == null) {
            binding = monitor.getBindings().get(CommandActionsGame.getGameBinding(action));
        }

        if (binding != null) {
            executor.executeBindingWithHold(binding, holdTime);
            log.info("Executed action: {} with key: {}", action, binding);
        } else {
            log.warn("No binding found for action: {}", action);
            EventBusManager.publish(new VoiceProcessEvent("Custom command operator. No key binding found for action " + action));
        }
    }
}

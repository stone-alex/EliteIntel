package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.ai.hands.KeyBindingExecutor;
import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CustomCommandOperator serves as a base class for handling custom keyboard operations
 * tied to game-related commands. It facilitates executing key bindings with a specified
 * hold duration and logs the results of the execution.
 */
public class CommandOperator {
    private static final Logger log = LogManager.getLogger(CommandOperator.class);
    private final BindingsMonitor monitor;
    private final KeyBindingExecutor executor;

    public CommandOperator(BindingsMonitor monitor, KeyBindingExecutor executor) {
        this.monitor = monitor;
        this.executor = executor;
    }


    public void operateKeyboard(String bindingIdentifier, int holdTime) {
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(bindingIdentifier);

        if (binding != null) {
            executor.executeBindingWithHold(binding, holdTime);
        } else {
            log.warn("No binding found for action: {}", bindingIdentifier);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Custom command operator. No key binding found for " + bindingIdentifier));
        }
    }

    /**
     * Executes a key binding as a guaranteed single tap, ignoring any hold="1"
     * attribute in the binding XML. Use this for UI navigation (tab cycling, panel
     * open/close) where a hold would trigger key-repeat and fire multiple inputs.
     */
    public void operateKeyboardTap(String bindingIdentifier) {
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(bindingIdentifier);

        if (binding != null) {
            log.debug("Tap binding: key={}, ignoring hold flag={}", binding.key, binding.hold);
            executor.executeTap(binding);
        } else {
            log.warn("No binding found for action: {}", bindingIdentifier);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Custom command operator. No key binding found for " + bindingIdentifier));
        }
    }
}
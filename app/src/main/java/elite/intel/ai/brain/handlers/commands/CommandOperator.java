package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.ai.hands.KeyBindingExecutor;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.hands.events.GameTapEvent;
import elite.intel.gameapi.GameControllerBus;
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


    /**
     * Executes a key binding associated with the specified identifier by holding the key
     * for the provided duration. If the binding is not found, a warning is logged,
     * and an announcement event is published.
     * <p>
     * Applies a random delay after the tap.
     *
     * @param bindingIdentifier The identifier used to retrieve the corresponding key binding.
     *                          Typically corresponds to a defined action in the key bindings.
     * @param holdTime          The duration in milliseconds for which the key is held down while
     *                          executing the binding.
     */
    public void operateKeyboard(String bindingIdentifier, int holdTime) {
        GameControllerBus.publish(new GameInputEvent(bindingIdentifier, holdTime));
    }

    /**
     * Executes a key binding as a guaranteed single tap, ignoring any hold="1"
     * attribute in the binding XML. Use this for UI navigation (tab cycling, panel
     * open/close) where a hold would trigger key-repeat and fire multiple inputs.
     *
     * Applies a random delay after tap.
     */
    public void operateKeyboardTap(String bindingIdentifier) {
        GameControllerBus.publish(new GameTapEvent(bindingIdentifier));
    }
}
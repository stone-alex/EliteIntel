package elite.companion.comms.handlers.command;

import elite.companion.comms.ai.robot.BindingsMonitor;
import elite.companion.comms.ai.robot.KeyBindingExecutor;
import elite.companion.comms.ai.robot.KeyBindingsParser;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.util.EventBusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

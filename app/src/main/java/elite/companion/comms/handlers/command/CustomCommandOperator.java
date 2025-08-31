package elite.companion.comms.handlers.command;

import elite.companion.comms.ai.robot.BindingsMonitor;
import elite.companion.comms.ai.robot.KeyBindingExecutor;
import elite.companion.comms.ai.robot.KeyBindingsParser;
import elite.companion.comms.voice.VoiceGenerator;
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


    protected void operateKeyboard(String actionSetThrottleToZero, int holdTime) {
        KeyBindingsParser.KeyBinding binding = monitor.getBindings().get(actionSetThrottleToZero);
        if (binding == null) {
            binding = monitor.getBindings().get(CommandActionsGame.getGameBinding(actionSetThrottleToZero));
        }

        if (binding != null) {
            executor.executeBindingWithHold(binding, holdTime);
            log.info("Executed action: {} with key: {}", actionSetThrottleToZero, binding);
        } else {
            log.warn("No binding found for action: {}", actionSetThrottleToZero);
            VoiceGenerator.getInstance().speak("No key binding found for that action.");
        }
    }
}

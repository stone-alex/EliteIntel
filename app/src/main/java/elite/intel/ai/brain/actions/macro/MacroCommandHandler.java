package elite.intel.ai.brain.actions.macro;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.actions.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Executes a {@link MacroDefinition} step-by-step.
 * <p>
 * Always invoked from the background thread already spawned by {@code ResponseRouter.handleCommand()},
 * so blocking {@link Thread#sleep} in {@code DELAY} steps is safe.
 */
public final class MacroCommandHandler implements CommandHandler {

    private static final Logger log = LogManager.getLogger(MacroCommandHandler.class);
    private final MacroDefinition macro;

    public MacroCommandHandler(MacroDefinition macro) {
        this.macro = macro;
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        log.info("Executing macro '{}' ({} step(s))", macro.getName(), macro.getSteps().size());
        for (int i = 0; i < macro.getSteps().size(); i++) {
            MacroStep step = macro.getSteps().get(i);
            try {
                executeStep(step, i);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Macro '{}' interrupted at step {}", macro.getName(), i);
                return;
            } catch (Exception e) {
                log.error("Macro '{}' step {} ({}) failed: {}", macro.getName(), i, step.getType(), e.getMessage(), e);
                // continue to next step rather than aborting the whole macro
            }
        }
        log.debug("Macro '{}' completed", macro.getName());
    }

    private void executeStep(MacroStep step, int index) throws InterruptedException {
        switch (step.getType()) {
            case BINDING_TAP ->
                GameControllerBus.publish(
                    GameInputSequenceEvent.single(GameInputStep.bindingTap(step.getBindingId())));

            case BINDING_HOLD ->
                GameControllerBus.publish(
                    GameInputSequenceEvent.single(GameInputStep.bindingHold(step.getBindingId(), step.getDurationMs())));

            case DELAY -> {
                log.debug("Macro '{}' step {}: delay {}ms", macro.getName(), index, step.getDurationMs());
                Thread.sleep(step.getDurationMs());
            }

            case SPEAK ->
                EventBusManager.publish(new AiVoxResponseEvent(step.getText()));

            case RUN_COMMAND -> {
                CommandHandler nested = CommandHandlerFactory.getInstance()
                        .getCommandHandlers()
                        .get(step.getActionId());
                if (nested == null) {
                    log.warn("Macro '{}' step {}: unknown actionId '{}' - step skipped",
                            macro.getName(), index, step.getActionId());
                } else if (nested instanceof MacroCommandHandler) {
                    // Prevent cross-macro delegation - macros must not call other macros.
                    log.warn("Macro '{}' step {}: RUN_COMMAND may not target another macro ('{}') - step skipped",
                            macro.getName(), index, step.getActionId());
                } else {
                    nested.handle(step.getActionId(), new JsonObject(), "");
                }
            }
        }
    }
}

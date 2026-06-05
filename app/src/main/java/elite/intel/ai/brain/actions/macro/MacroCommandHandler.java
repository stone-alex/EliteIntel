package elite.intel.ai.brain.actions.macro;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.actions.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import elite.intel.ai.hands.KeyBindingExecutor;
import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.ui.event.AppLogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;

/**
 * Executes a {@link MacroDefinition} step-by-step.
 * <p>
 * Always invoked from the background thread already spawned by {@code ResponseRouter.handleCommand()},
 * so blocking operations (DELAY sleep, SPEAK wait) are safe.
 * <p>
 * A single static fair {@link ReentrantLock} serializes <em>all</em> macro executions globally
 * (not per-macro) so that two macros triggered nearly simultaneously cannot interleave their
 * input or speech steps — from the user's perspective each macro must run atomically end-to-end.
 * Fair ordering (FIFO) ensures the second macro starts only after the first completes.
 * The lock is released in {@code finally} regardless of error or interruption.
 */
public final class MacroCommandHandler implements CommandHandler {

    private static final Logger log = LogManager.getLogger(MacroCommandHandler.class);

    /** Serializes all macro executions. Fair ordering ensures FIFO execution when macros queue up. */
    private static final ReentrantLock MACRO_LOCK = new ReentrantLock(true);

    private final MacroDefinition macro;
    private final MacroSpeakExecutor speakExecutor;

    public MacroCommandHandler(MacroDefinition macro) {
        this(macro, SynchronousMacroSpeech.DEFAULT);
    }

    /** Package-private: allows tests to inject a fast non-blocking speak executor. */
    MacroCommandHandler(MacroDefinition macro, MacroSpeakExecutor speakExecutor) {
        this.macro = macro;
        this.speakExecutor = speakExecutor;
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        MACRO_LOCK.lock();
        try {
            MacroExecutionContext ctx = MacroExecutionContext.fromJson(macro, params);
            List<String> paramErrors = ctx.validateRequiredParams();
            if (!paramErrors.isEmpty()) {
                String errorSummary = String.join(", ", paramErrors);
                log.warn("Macro '{}' aborted: {}", macro.getName(), errorSummary);
                EventBusManager.publish(new AppLogEvent("Macro '" + macro.getName() + "' aborted: " + errorSummary));
                return;
            }
            log.info("Executing macro '{}' ({} step(s))", macro.getName(), macro.getSteps().size());
            PendingInputSequence pendingInput = new PendingInputSequence();
            for (int i = 0; i < macro.getSteps().size(); i++) {
                MacroStep step = macro.getSteps().get(i);
                try {
                    executeStep(step, i, pendingInput, ctx);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Macro '{}' interrupted at step {}", macro.getName(), i);
                    return;
                } catch (UnresolvedMacroParamException e) {
                    log.error("Macro '{}' step {} ({}): {} — step skipped",
                            macro.getName(), i, step.getType(), e.getMessage());
                    EventBusManager.publish(new AppLogEvent(
                            "Macro step error: " + e.getMessage() + " (step skipped)"));
                } catch (Exception e) {
                    log.error("Macro '{}' step {} ({}) failed: {}", macro.getName(), i, step.getType(), e.getMessage(), e);
                    // continue to next step rather than aborting the whole macro
                }
            }
            try {
                flushPendingInputSteps(pendingInput);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Macro '{}' interrupted while flushing input sequence", macro.getName());
                return;
            }
            log.debug("Macro '{}' completed", macro.getName());
        } finally {
            MACRO_LOCK.unlock();
        }
    }

    private void executeStep(MacroStep step, int index, PendingInputSequence pendingInput,
                             MacroExecutionContext ctx) throws InterruptedException {
        switch (step.getType()) {
            case BINDING_TAP -> {
                EventBusManager.publish(new AppLogEvent("Macro step: BINDING_TAP " + step.getBindingId()));
                pendingInput.addInput(GameInputStep.bindingTap(step.getBindingId()));
            }

            case BINDING_HOLD -> {
                EventBusManager.publish(new AppLogEvent("Macro step: BINDING_HOLD " + step.getBindingId() + " " + step.getDurationMs() + "ms"));
                pendingInput.addInput(GameInputStep.bindingHold(step.getBindingId(), step.getDurationMs()));
            }

            case DELAY -> {
                EventBusManager.publish(new AppLogEvent("Macro step: DELAY " + step.getDurationMs() + "ms"));
                pendingInput.addDelay(GameInputStep.delay(step.getDurationMs()));
            }

            case SPEAK -> {
                // Flush accumulated input before speaking so keystrokes reach the game first.
                flushPendingInputSteps(pendingInput);
                String resolvedText = ctx.resolveString(step.getText());
                EventBusManager.publish(new AppLogEvent("Macro step: SPEAK " + resolvedText));
                speakExecutor.speak(resolvedText);
            }

            case RAW_KEY -> {
                Integer keyCode = KeyBindingExecutor.resolveKeyCode(step.getRawKey());
                if (keyCode == null) {
                    log.warn("Macro '{}' step {}: unknown rawKey '{}' — step skipped",
                            macro.getName(), index, step.getRawKey());
                    EventBusManager.publish(new AppLogEvent(
                            "Macro step: RAW_KEY " + step.getRawKey() + " (unknown key - skipped)"));
                    break;
                }
                int modCode = 0;
                String rawMod = step.getRawKeyModifier();
                if (rawMod != null && !rawMod.isBlank()) {
                    Integer resolved = KeyBindingExecutor.resolveKeyCode(rawMod);
                    if (resolved == null) {
                        log.warn("Macro '{}' step {}: unknown rawKeyModifier '{}' — executing without modifier",
                                macro.getName(), index, rawMod);
                    } else {
                        modCode = resolved;
                    }
                }
                String logSuffix = (modCode != 0 ? " + " + rawMod : "")
                        + (step.getDurationMs() > 0 ? " " + step.getDurationMs() + "ms" : "");
                EventBusManager.publish(new AppLogEvent("Macro step: RAW_KEY " + step.getRawKey() + logSuffix));
                pendingInput.addInput(GameInputStep.rawKey(keyCode, modCode, step.getDurationMs()));
            }

            case RUN_COMMAND -> {
                // Flush before delegating so pending keystrokes are sent before the nested handler runs.
                flushPendingInputSteps(pendingInput);
                CommandHandler nested = CommandHandlerFactory.getInstance()
                        .getCommandHandlers()
                        .get(step.getActionId());
                if (nested == null) {
                    log.warn("Macro '{}' step {}: unknown actionId '{}' - step skipped",
                            macro.getName(), index, step.getActionId());
                    EventBusManager.publish(new AppLogEvent("Macro step: RUN_COMMAND " + step.getActionId() + " (unknown - skipped)"));
                } else if (nested instanceof MacroCommandHandler) {
                    // Prevent cross-macro delegation - macros must not call other macros.
                    log.warn("Macro '{}' step {}: RUN_COMMAND may not target another macro ('{}') - step skipped",
                            macro.getName(), index, step.getActionId());
                    EventBusManager.publish(new AppLogEvent("Macro step: RUN_COMMAND " + step.getActionId() + " (cross-macro blocked)"));
                } else {
                    // Resolve step-level param mapping; preserves JSON types for bare ${ref} values.
                    Map<String, String> stepParamMapping = step.getStepParams();
                    JsonObject resolvedParams = ctx.resolveStepParams(stepParamMapping);
                    EventBusManager.publish(new AppLogEvent("Macro step: RUN_COMMAND " + step.getActionId()));
                    nested.handle(step.getActionId(), resolvedParams, "");
                }
            }
        }
    }

    private void flushPendingInputSteps(PendingInputSequence pendingInput) throws InterruptedException {
        if (pendingInput.isEmpty()) {
            return;
        }
        if (pendingInput.hasInput()) {
            GameControllerBus.publish(new GameInputSequenceEvent(pendingInput.steps()));
        } else {
            for (GameInputStep step : pendingInput.steps()) {
                Thread.sleep(step.getDurationMs());
            }
        }
        pendingInput.clear();
    }

    /**
     * Accumulates input steps (bindings, raw keys) and delays before publishing them as a single
     * {@link GameInputSequenceEvent}. Delays without any real input are executed via
     * {@link Thread#sleep} instead, since the executor does not add inter-step pauses for delay-only sequences.
     */
    private static final class PendingInputSequence {
        private final List<GameInputStep> steps = new ArrayList<>();
        private boolean hasInput;

        void addInput(GameInputStep step) {
            steps.add(step);
            hasInput = true;
        }

        void addDelay(GameInputStep step) {
            steps.add(step);
        }

        boolean isEmpty() {
            return steps.isEmpty();
        }

        boolean hasInput() {
            return hasInput;
        }

        List<GameInputStep> steps() {
            return steps;
        }

        void clear() {
            steps.clear();
            hasInput = false;
        }
    }
}

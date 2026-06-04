package elite.intel.ai.brain.actions.macro;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.actions.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class MacroCommandHandlerTest {

    private static final Gson GSON = new Gson();

    private final InputCapture inputCapture = new InputCapture();
    private final VoxCapture voxCapture = new VoxCapture();

    // Keys added to CommandHandlerFactory in individual tests - cleaned up in @AfterEach.
    private final List<String> addedHandlerKeys = new ArrayList<>();

    @BeforeEach
    void registerCaptures() {
        GameControllerBus.register(inputCapture);
        EventBusManager.register(voxCapture);
    }

    @AfterEach
    void cleanUp() {
        GameControllerBus.unregister(inputCapture);
        EventBusManager.unregister(voxCapture);
        inputCapture.events.clear();
        voxCapture.events.clear();
        for (String key : addedHandlerKeys) {
            CommandHandlerFactory.getInstance().getCommandHandlers().remove(key);
        }
        addedHandlerKeys.clear();
    }

    // --- BINDING_TAP ---

    @Test
    void bindingTapStepPublishesGameInputWithCorrectBindingId() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"TestBinding"}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        GameInputStep step = inputCapture.events.getFirst().getSteps().getFirst();
        assertEquals(GameInputStep.Type.BINDING_TAP, step.getType());
        assertEquals("TestBinding", step.getBindingId());
    }

    // --- BINDING_HOLD ---

    @Test
    void bindingHoldStepPreservesBindingIdAndDuration() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"BINDING_HOLD","bindingId":"HoldBinding","durationMs":300}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        GameInputStep step = inputCapture.events.getFirst().getSteps().getFirst();
        assertEquals(GameInputStep.Type.BINDING_HOLD, step.getType());
        assertEquals("HoldBinding", step.getBindingId());
        assertEquals(300, step.getDurationMs());
    }

    // --- DELAY ---

    @Test
    void delayStepProducesNoInputEvents() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"DELAY","durationMs":0}
                ]}""");

        assertTrue(inputCapture.events.isEmpty());
        assertTrue(voxCapture.events.isEmpty());
    }

    // --- SPEAK ---

    @Test
    void speakStepPublishesAiVoxEventWithCorrectText() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"SPEAK","text":"Hello pilot"}
                ]}""");

        assertEquals(1, voxCapture.events.size());
        assertEquals("Hello pilot", voxCapture.events.getFirst().getText());
        assertTrue(inputCapture.events.isEmpty());
    }

    // --- RUN_COMMAND ---

    @Test
    void runCommandStepDelegatesToRegisteredBuiltinHandler() {
        AtomicBoolean called = new AtomicBoolean(false);
        CommandHandler fakeBuiltin = (a, p, r) -> called.set(true);
        registerHandler("builtin_action", fakeBuiltin);

        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"RUN_COMMAND","actionId":"builtin_action"}
                ]}""");

        assertTrue(called.get(), "Builtin handler must be called");
    }

    @Test
    void runCommandStepSkipsWhenTargetHandlerIsMacroCommandHandler() {
        // Register a second macro handler as the RUN_COMMAND target.
        MacroDefinition nested = deserialize("""
                {"id":"macro_nested","name":"Nested","phrases":"p",
                 "steps":[{"type":"SPEAK","text":"nested called"}]}""");
        MacroCommandHandler nestedHandler = new MacroCommandHandler(nested);
        registerHandler("macro_nested", nestedHandler);

        // Macro that tries to call another macro.
        runMacro("""
                {"id":"macro_caller","name":"Caller","phrases":"p","steps":[
                  {"type":"RUN_COMMAND","actionId":"macro_nested"}
                ]}""");

        // Guard must prevent the nested macro from running - no speech from nested.
        assertTrue(voxCapture.events.isEmpty(), "Cross-macro call must be blocked");
    }

    @Test
    void runCommandStepSkipsUnknownActionId() {
        // No exception - just logs a warning.
        assertDoesNotThrow(() -> runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"RUN_COMMAND","actionId":"does_not_exist_12345"}
                ]}"""));
        assertTrue(inputCapture.events.isEmpty());
    }

    // --- multi-step ordering ---

    @Test
    void multiStepMacroProducesEventsInOrder() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"FirstBinding"},
                  {"type":"DELAY","durationMs":0},
                  {"type":"SPEAK","text":"sequence done"}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        assertEquals("FirstBinding",
                inputCapture.events.getFirst().getSteps().getFirst().getBindingId());
        assertEquals(1, voxCapture.events.size());
        assertEquals("sequence done", voxCapture.events.getFirst().getText());
    }

    // --- interrupted thread ---

    @Test
    void interruptedThreadStopsAfterCurrentStep() throws InterruptedException {
        // We run the handler on a separate thread so we can interrupt it.
        MacroDefinition macro = deserialize("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"DELAY","durationMs":5000},
                  {"type":"SPEAK","text":"should not reach here"}
                ]}""");
        MacroCommandHandler handler = new MacroCommandHandler(macro);

        Thread t = new Thread(() -> handler.handle("m", new JsonObject(), ""));
        t.start();
        // Give the handler time to enter the sleep, then interrupt.
        Thread.sleep(50);
        t.interrupt();
        t.join(2000);

        assertFalse(t.isAlive(), "Handler thread must exit after interrupt");
        assertTrue(voxCapture.events.isEmpty(), "SPEAK after interrupted DELAY must not fire");
    }

    // --- helpers ---

    private void runMacro(String json) {
        MacroCommandHandler handler = new MacroCommandHandler(deserialize(json));
        handler.handle("test_action", new JsonObject(), "");
    }

    private MacroDefinition deserialize(String json) {
        return GSON.fromJson(json, MacroDefinition.class);
    }

    private void registerHandler(String key, CommandHandler handler) {
        CommandHandlerFactory.getInstance().getCommandHandlers().put(key, handler);
        addedHandlerKeys.add(key);
    }

    // --- event capture subscribers ---

    private static class InputCapture {
        final List<GameInputSequenceEvent> events = new ArrayList<>();

        @Subscribe
        public void on(GameInputSequenceEvent e) {
            events.add(e);
        }
    }

    private static class VoxCapture {
        final List<AiVoxResponseEvent> events = new ArrayList<>();

        @Subscribe
        public void on(AiVoxResponseEvent e) {
            events.add(e);
        }
    }
}

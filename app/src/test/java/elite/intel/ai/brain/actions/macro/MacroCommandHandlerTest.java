package elite.intel.ai.brain.actions.macro;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.intel.ai.brain.actions.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import elite.intel.ai.hands.KeyBindingExecutor;
import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;
import elite.intel.gameapi.GameControllerBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MacroCommandHandlerTest {

    private static final Gson GSON = new Gson();

    private final InputCapture inputCapture = new InputCapture();
    private final TestSpeakExecutor testSpeakExecutor = new TestSpeakExecutor();

    // Keys added to CommandHandlerFactory in individual tests - cleaned up in @AfterEach.
    private final List<String> addedHandlerKeys = new ArrayList<>();

    @BeforeEach
    void registerCaptures() {
        GameControllerBus.register(inputCapture);
    }

    @AfterEach
    void cleanUp() {
        GameControllerBus.unregister(inputCapture);
        inputCapture.events.clear();
        testSpeakExecutor.spoken.clear();
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
        assertTrue(testSpeakExecutor.spoken.isEmpty());
    }

    // --- SPEAK ---

    @Test
    void speakStepCallsSpeakExecutorWithCorrectText() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"SPEAK","text":"Hello pilot"}
                ]}""");

        assertEquals(1, testSpeakExecutor.spoken.size());
        assertEquals("Hello pilot", testSpeakExecutor.spoken.getFirst());
        assertTrue(inputCapture.events.isEmpty());
    }

    @Test
    void speakBlocksUntilExecutorCompletes() throws InterruptedException {
        CountDownLatch speakStarted = new CountDownLatch(1);
        CountDownLatch speakRelease = new CountDownLatch(1);

        MacroSpeakExecutor blockingExecutor = text -> {
            speakStarted.countDown();
            speakRelease.await();
        };

        MacroDefinition macro = deserialize("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"SPEAK","text":"Wait for me"},
                  {"type":"BINDING_TAP","bindingId":"AfterSpeak"}
                ]}""");
        MacroCommandHandler handler = new MacroCommandHandler(macro, blockingExecutor);

        Thread t = new Thread(() -> handler.handle("m", new JsonObject(), ""));
        t.start();

        assertTrue(speakStarted.await(2, TimeUnit.SECONDS), "SPEAK must start within 2s");
        assertTrue(inputCapture.events.isEmpty(), "BINDING_TAP must not fire while SPEAK is executing");

        speakRelease.countDown();
        t.join(2000);

        assertEquals(1, inputCapture.events.size(), "BINDING_TAP must fire after SPEAK completes");
        assertEquals("AfterSpeak", inputCapture.events.getFirst().getSteps().getFirst().getBindingId());
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
        MacroCommandHandler nestedHandler = new MacroCommandHandler(nested, testSpeakExecutor);
        registerHandler("macro_nested", nestedHandler);

        // Macro that tries to call another macro.
        runMacro("""
                {"id":"macro_caller","name":"Caller","phrases":"p","steps":[
                  {"type":"RUN_COMMAND","actionId":"macro_nested"}
                ]}""");

        // Guard must prevent the nested macro from running - no speech from nested.
        assertTrue(testSpeakExecutor.spoken.isEmpty(), "Cross-macro call must be blocked");
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

    // --- RAW_KEY ---

    @Test
    void rawKeyStepPublishesGameInputWithCorrectKeyCode() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"RAW_KEY","rawKey":"KEY_F5","durationMs":0}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        GameInputStep step = inputCapture.events.getFirst().getSteps().getFirst();
        assertEquals(GameInputStep.Type.RAW_KEY, step.getType());
        assertEquals(KeyBindingExecutor.resolveKeyCode("KEY_F5"), step.getKeyCode());
        assertEquals(0, step.getModifierKeyCode());
        assertEquals(0, step.getDurationMs());
    }

    @Test
    void rawKeyStepWithModifierSetsModifierKeyCode() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"RAW_KEY","rawKey":"KEY_W","rawKeyModifier":"KEY_LEFTCONTROL","durationMs":0}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        GameInputStep step = inputCapture.events.getFirst().getSteps().getFirst();
        assertEquals(GameInputStep.Type.RAW_KEY, step.getType());
        assertEquals(KeyBindingExecutor.resolveKeyCode("KEY_W"), step.getKeyCode());
        assertEquals(KeyBindingExecutor.resolveKeyCode("KEY_LEFTCONTROL"), step.getModifierKeyCode());
        assertEquals(0, step.getDurationMs());
    }

    @Test
    void rawKeyStepWithHoldDurationPreservesDuration() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"RAW_KEY","rawKey":"KEY_SPACE","durationMs":500}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        GameInputStep step = inputCapture.events.getFirst().getSteps().getFirst();
        assertEquals(GameInputStep.Type.RAW_KEY, step.getType());
        assertEquals(KeyBindingExecutor.resolveKeyCode("KEY_SPACE"), step.getKeyCode());
        assertEquals(500, step.getDurationMs());
    }

    @Test
    void rawKeyStepWithUnknownKeyIsSkipped() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"RAW_KEY","rawKey":"KEY_DOES_NOT_EXIST_99999","durationMs":0}
                ]}""");

        assertTrue(inputCapture.events.isEmpty());
    }

    @Test
    void rawKeyStepWithUnknownModifierFallsBackToNoModifier() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"RAW_KEY","rawKey":"KEY_W","rawKeyModifier":"KEY_BAD_MODIFIER","durationMs":0}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        GameInputStep step = inputCapture.events.getFirst().getSteps().getFirst();
        assertEquals(GameInputStep.Type.RAW_KEY, step.getType());
        // Unknown modifier resolves to 0 (no modifier) rather than skipping the step
        assertEquals(0, step.getModifierKeyCode());
        assertEquals(KeyBindingExecutor.resolveKeyCode("KEY_W"), step.getKeyCode());
    }

    @Test
    void rawKeyStepCaseInsensitiveKeyName() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"RAW_KEY","rawKey":"Key_LeftControl","durationMs":0}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        GameInputStep step = inputCapture.events.getFirst().getSteps().getFirst();
        assertEquals(KeyBindingExecutor.resolveKeyCode("KEY_LEFTCONTROL"), step.getKeyCode());
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
        List<GameInputStep> steps = inputCapture.events.getFirst().getSteps();
        assertEquals(2, steps.size());
        assertEquals("FirstBinding", steps.getFirst().getBindingId());
        assertEquals(GameInputStep.Type.DELAY, steps.get(1).getType());
        assertEquals(1, testSpeakExecutor.spoken.size());
        assertEquals("sequence done", testSpeakExecutor.spoken.getFirst());
    }

    @Test
    void speakDelayBindingSequenceStillPublishesFinalBindingTap() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"SPEAK","text":"Hello pilot"},
                  {"type":"DELAY","durationMs":0},
                  {"type":"BINDING_TAP","bindingId":"GalaxyMapOpen"}
                ]}""");

        assertEquals(1, testSpeakExecutor.spoken.size());
        assertEquals("Hello pilot", testSpeakExecutor.spoken.getFirst());
        assertEquals(1, inputCapture.events.size());
        List<GameInputStep> steps = inputCapture.events.getFirst().getSteps();
        assertEquals(2, steps.size());
        assertEquals(GameInputStep.Type.DELAY, steps.getFirst().getType());
        GameInputStep step = steps.get(1);
        assertEquals(GameInputStep.Type.BINDING_TAP, step.getType());
        assertEquals("GalaxyMapOpen", step.getBindingId());
    }

    @Test
    void bindingDelayBindingPublishesSingleSequenceInOrder() {
        runMacro("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"FirstBinding"},
                  {"type":"DELAY","durationMs":250},
                  {"type":"BINDING_TAP","bindingId":"SecondBinding"}
                ]}""");

        assertEquals(1, inputCapture.events.size());
        List<GameInputStep> steps = inputCapture.events.getFirst().getSteps();
        assertEquals(3, steps.size());
        assertEquals("FirstBinding", steps.get(0).getBindingId());
        assertEquals(GameInputStep.Type.DELAY, steps.get(1).getType());
        assertEquals(250, steps.get(1).getDurationMs());
        assertEquals("SecondBinding", steps.get(2).getBindingId());
    }

    // --- macro atomicity: two macros must not interleave ---

    @Test
    void twoMacrosDoNotInterleaveInputSteps() throws InterruptedException {
        MacroDefinition macro1 = deserialize("""
                {"id":"m1","name":"M1","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"Macro1Step1"},
                  {"type":"DELAY","durationMs":0},
                  {"type":"BINDING_TAP","bindingId":"Macro1Step2"}
                ]}""");
        MacroDefinition macro2 = deserialize("""
                {"id":"m2","name":"M2","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"Macro2Step1"},
                  {"type":"DELAY","durationMs":0},
                  {"type":"BINDING_TAP","bindingId":"Macro2Step2"}
                ]}""");

        MacroCommandHandler h1 = new MacroCommandHandler(macro1, testSpeakExecutor);
        MacroCommandHandler h2 = new MacroCommandHandler(macro2, testSpeakExecutor);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            ready.countDown();
            try { go.await(); } catch (InterruptedException e) { return; }
            h1.handle("m1", new JsonObject(), "");
        });
        Thread t2 = new Thread(() -> {
            ready.countDown();
            try { go.await(); } catch (InterruptedException e) { return; }
            h2.handle("m2", new JsonObject(), "");
        });

        t1.start();
        t2.start();
        assertTrue(ready.await(2, TimeUnit.SECONDS));
        go.countDown();
        t1.join(5000);
        t2.join(5000);

        assertEquals(2, inputCapture.events.size(), "Each macro must produce exactly one GameInputSequenceEvent");

        // No interleaving: each event's bindings must all belong to the same macro
        List<String> first = inputCapture.events.get(0).getSteps().stream()
                .filter(s -> s.getType() != GameInputStep.Type.DELAY)
                .map(GameInputStep::getBindingId)
                .toList();
        List<String> second = inputCapture.events.get(1).getSteps().stream()
                .filter(s -> s.getType() != GameInputStep.Type.DELAY)
                .map(GameInputStep::getBindingId)
                .toList();

        boolean firstIsMacro1 = first.stream().allMatch(b -> b.startsWith("Macro1"));
        boolean firstIsMacro2 = first.stream().allMatch(b -> b.startsWith("Macro2"));
        assertTrue(firstIsMacro1 || firstIsMacro2, "First event must belong entirely to one macro");

        if (firstIsMacro1) {
            assertTrue(second.stream().allMatch(b -> b.startsWith("Macro2")), "Second event must be Macro2's steps");
        } else {
            assertTrue(second.stream().allMatch(b -> b.startsWith("Macro1")), "Second event must be Macro1's steps");
        }
    }

    // --- parameterized macros ---

    @Test
    void runCommandStepPassesResolvedStepParamsToNestedHandler() {
        AtomicReference<JsonObject> capturedParams = new AtomicReference<>();
        CommandHandler fakeBuiltin = (a, p, r) -> capturedParams.set(p);
        registerHandler("builtin_with_params", fakeBuiltin);

        MacroDefinition macro = new MacroDefinition(
                "m", "M", "", "phrase",
                List.of(new MacroParameterSpec("commodity", "string", true, "", null, null)),
                List.of(MacroStep.runCommandWithParams("builtin_with_params", Map.of("key", "${commodity}")))
        );
        MacroCommandHandler handler = new MacroCommandHandler(macro, testSpeakExecutor);
        JsonObject params = JsonParser.parseString("{\"commodity\": \"gold\"}").getAsJsonObject();
        handler.handle("m", params, "");

        assertNotNull(capturedParams.get(), "Nested handler must be called with resolved params");
        assertEquals("gold", capturedParams.get().get("key").getAsString());
    }

    @Test
    void runCommandStepPreservesJsonNumberType() {
        AtomicReference<JsonObject> capturedParams = new AtomicReference<>();
        CommandHandler fakeBuiltin = (a, p, r) -> capturedParams.set(p);
        registerHandler("navigate_fake", fakeBuiltin);

        MacroDefinition macro = new MacroDefinition(
                "m", "M", "", "phrase",
                List.of(
                        new MacroParameterSpec("lat", "number", true, "", null, null),
                        new MacroParameterSpec("lon", "number", true, "", null, null)
                ),
                List.of(MacroStep.runCommandWithParams("navigate_fake", Map.of("lat", "${lat}", "lon", "${lon}")))
        );
        MacroCommandHandler handler = new MacroCommandHandler(macro, testSpeakExecutor);
        JsonObject params = JsonParser.parseString("{\"lat\": -10.5, \"lon\": 45.2}").getAsJsonObject();
        handler.handle("m", params, "");

        assertNotNull(capturedParams.get());
        assertEquals(-10.5, capturedParams.get().get("lat").getAsDouble(), 0.001);
        assertEquals(45.2, capturedParams.get().get("lon").getAsDouble(), 0.001);
    }

    @Test
    void abortsMacroWhenRequiredParamIsMissing() {
        AtomicBoolean called = new AtomicBoolean(false);
        CommandHandler fakeBuiltin = (a, p, r) -> called.set(true);
        registerHandler("cmd_fake", fakeBuiltin);

        MacroDefinition macro = new MacroDefinition(
                "m", "M", "", "phrase",
                List.of(new MacroParameterSpec("speed", "string", true, "", null, null)),
                List.of(MacroStep.runCommandWithParams("cmd_fake", Map.of("key", "${speed}")))
        );
        MacroCommandHandler handler = new MacroCommandHandler(macro, testSpeakExecutor);
        // No params provided — required "speed" is missing.
        handler.handle("m", new JsonObject(), "");

        assertFalse(called.get(), "Macro must be aborted when required param is missing");
    }

    @Test
    void speakStepResolvesParamTemplate() {
        MacroDefinition macro = new MacroDefinition(
                "m", "M", "", "phrase",
                List.of(new MacroParameterSpec("target", "string", true, "", null, null)),
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "Targeting ${target}", null))
        );
        MacroCommandHandler handler = new MacroCommandHandler(macro, testSpeakExecutor);
        JsonObject params = JsonParser.parseString("{\"target\": \"drive\"}").getAsJsonObject();
        handler.handle("m", params, "");

        assertEquals(1, testSpeakExecutor.spoken.size());
        assertEquals("Targeting drive", testSpeakExecutor.spoken.getFirst());
    }

    @Test
    void optionalParamAbsentDoesNotAbortMacro() {
        AtomicBoolean called = new AtomicBoolean(false);
        CommandHandler fakeBuiltin = (a, p, r) -> called.set(true);
        registerHandler("cmd_optional", fakeBuiltin);

        MacroDefinition macro = new MacroDefinition(
                "m", "M", "", "phrase",
                List.of(new MacroParameterSpec("hint", "string", false, "", null, null)),
                // step doesn't use the optional param at all
                List.of(new MacroStep(MacroStep.Type.RUN_COMMAND, null, 0, null, "cmd_optional"))
        );
        MacroCommandHandler handler = new MacroCommandHandler(macro, testSpeakExecutor);
        handler.handle("m", new JsonObject(), "");

        assertTrue(called.get(), "Macro with absent optional param must still execute");
    }

    @Test
    void macroLockReleasedAfterInterrupt() throws InterruptedException {
        MacroDefinition slowMacro = deserialize("""
                {"id":"slow","name":"Slow","phrases":"p","steps":[
                  {"type":"DELAY","durationMs":5000}
                ]}""");
        MacroDefinition fastMacro = deserialize("""
                {"id":"fast","name":"Fast","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"FastBinding"}
                ]}""");

        MacroCommandHandler slowHandler = new MacroCommandHandler(slowMacro, testSpeakExecutor);
        MacroCommandHandler fastHandler = new MacroCommandHandler(fastMacro, testSpeakExecutor);

        Thread slowThread = new Thread(() -> slowHandler.handle("slow", new JsonObject(), ""));
        slowThread.start();
        Thread.sleep(50); // let slow macro acquire lock and enter DELAY
        slowThread.interrupt();
        slowThread.join(2000);

        // The lock must be released - the fast macro must now complete
        Thread fastThread = new Thread(() -> fastHandler.handle("fast", new JsonObject(), ""));
        fastThread.start();
        fastThread.join(2000);

        assertFalse(fastThread.isAlive(), "Fast macro must complete after slow macro is interrupted");
        assertEquals(1, inputCapture.events.size());
        assertEquals("FastBinding", inputCapture.events.getFirst().getSteps().getFirst().getBindingId());
    }

    // --- interrupted thread ---

    @Test
    void interruptedThreadStopsAfterCurrentStep() throws InterruptedException {
        MacroDefinition macro = deserialize("""
                {"id":"m","name":"M","phrases":"p","steps":[
                  {"type":"DELAY","durationMs":5000},
                  {"type":"SPEAK","text":"should not reach here"}
                ]}""");
        MacroCommandHandler handler = new MacroCommandHandler(macro, testSpeakExecutor);

        Thread t = new Thread(() -> handler.handle("m", new JsonObject(), ""));
        t.start();
        // Give the handler time to enter the sleep, then interrupt.
        Thread.sleep(50);
        t.interrupt();
        t.join(2000);

        assertFalse(t.isAlive(), "Handler thread must exit after interrupt");
        assertTrue(testSpeakExecutor.spoken.isEmpty(), "SPEAK after interrupted DELAY must not fire");
    }

    // --- helpers ---

    private void runMacro(String json) {
        MacroCommandHandler handler = new MacroCommandHandler(deserialize(json), testSpeakExecutor);
        handler.handle("test_action", new JsonObject(), "");
    }

    private MacroDefinition deserialize(String json) {
        return GSON.fromJson(json, MacroDefinition.class);
    }

    private void registerHandler(String key, CommandHandler handler) {
        CommandHandlerFactory.getInstance().getCommandHandlers().put(key, handler);
        addedHandlerKeys.add(key);
    }

    // --- test doubles ---

    /** Captures spoken texts without blocking or publishing events. */
    static class TestSpeakExecutor implements MacroSpeakExecutor {
        final List<String> spoken = new ArrayList<>();

        @Override
        public void speak(String text) {
            spoken.add(text);
        }
    }

    private static class InputCapture {
        final List<GameInputSequenceEvent> events = new ArrayList<>();

        @Subscribe
        public void on(GameInputSequenceEvent e) {
            events.add(e);
        }
    }
}

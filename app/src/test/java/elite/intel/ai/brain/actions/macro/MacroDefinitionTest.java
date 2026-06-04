package elite.intel.ai.brain.actions.macro;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MacroDefinitionTest {

    private static final Gson GSON = new Gson();

    // --- validate() ---

    @Test
    void validMacroPassesValidation() {
        MacroDefinition macro = deserialize("""
                {
                  "id": "macro_test",
                  "name": "Test",
                  "phrases": "test me",
                  "steps": [{"type": "SPEAK", "text": "hi"}]
                }
                """);
        assertDoesNotThrow(macro::validate);
    }

    @Test
    void blankIdIsRejected() {
        MacroDefinition macro = deserialize("""
                {"id": "", "name": "T", "phrases": "p", "steps": [{"type":"SPEAK","text":"x"}]}
                """);
        assertThrows(IllegalArgumentException.class, macro::validate);
    }

    @Test
    void blankNameIsRejected() {
        MacroDefinition macro = deserialize("""
                {"id": "macro_x", "name": "", "phrases": "p", "steps": [{"type":"SPEAK","text":"x"}]}
                """);
        assertThrows(IllegalArgumentException.class, macro::validate);
    }

    @Test
    void blankPhrasesIsRejected() {
        MacroDefinition macro = deserialize("""
                {"id": "macro_x", "name": "T", "phrases": "", "steps": [{"type":"SPEAK","text":"x"}]}
                """);
        assertThrows(IllegalArgumentException.class, macro::validate);
    }

    @Test
    void emptyStepsIsRejected() {
        MacroDefinition macro = deserialize("""
                {"id": "macro_x", "name": "T", "phrases": "p", "steps": []}
                """);
        assertThrows(IllegalArgumentException.class, macro::validate);
    }

    @Test
    void nullStepInsideListIsRejected() {
        MacroDefinition macro = deserialize("""
                {"id": "macro_x", "name": "T", "phrases": "p", "steps": [null]}
                """);
        assertThrows(IllegalArgumentException.class, macro::validate);
    }

    @Test
    void bindingTapStepWithoutBindingIdIsRejected() {
        MacroDefinition macro = deserialize("""
                {"id": "macro_x", "name": "T", "phrases": "p",
                 "steps": [{"type":"BINDING_TAP"}]}
                """);
        assertThrows(IllegalArgumentException.class, macro::validate);
    }

    @Test
    void speakStepWithoutTextIsRejected() {
        MacroDefinition macro = deserialize("""
                {"id": "macro_x", "name": "T", "phrases": "p",
                 "steps": [{"type":"SPEAK"}]}
                """);
        assertThrows(IllegalArgumentException.class, macro::validate);
    }

    @Test
    void descriptionIsOptionalAndDefaultsToEmpty() {
        MacroDefinition macro = deserialize("""
                {"id": "macro_x", "name": "T", "phrases": "p",
                 "steps": [{"type":"SPEAK","text":"ok"}]}
                """);
        assertDoesNotThrow(macro::validate);
        assertEquals("", macro.getDescription());
    }

    // --- distinctBindingIds() ---

    @Test
    void distinctBindingIdsForBindingTapAndHold() {
        MacroDefinition macro = deserialize("""
                {"id":"m","name":"N","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"A"},
                  {"type":"BINDING_HOLD","bindingId":"B","durationMs":200}
                ]}""");
        assertEquals(List.of("A", "B"), macro.distinctBindingIds());
    }

    @Test
    void distinctBindingIdsDeduplicates() {
        MacroDefinition macro = deserialize("""
                {"id":"m","name":"N","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"A"},
                  {"type":"BINDING_TAP","bindingId":"A"},
                  {"type":"BINDING_HOLD","bindingId":"B","durationMs":100}
                ]}""");
        assertEquals(List.of("A", "B"), macro.distinctBindingIds());
    }

    @Test
    void distinctBindingIdsExcludesDelayAndSpeakAndRunCommand() {
        MacroDefinition macro = deserialize("""
                {"id":"m","name":"N","phrases":"p","steps":[
                  {"type":"DELAY","durationMs":500},
                  {"type":"SPEAK","text":"hi"},
                  {"type":"RUN_COMMAND","actionId":"deploy_landing_gear"}
                ]}""");
        assertTrue(macro.distinctBindingIds().isEmpty());
    }

    @Test
    void distinctBindingIdsReturnsEmptyWhenNoBindingSteps() {
        MacroDefinition macro = deserialize("""
                {"id":"m","name":"N","phrases":"p","steps":[
                  {"type":"SPEAK","text":"hello"}
                ]}""");
        assertTrue(macro.distinctBindingIds().isEmpty());
    }

    @Test
    void distinctBindingIdsPreservesFirstOccurrenceOrder() {
        MacroDefinition macro = deserialize("""
                {"id":"m","name":"N","phrases":"p","steps":[
                  {"type":"BINDING_TAP","bindingId":"B"},
                  {"type":"BINDING_TAP","bindingId":"A"},
                  {"type":"BINDING_TAP","bindingId":"B"}
                ]}""");
        assertEquals(List.of("B", "A"), macro.distinctBindingIds());
    }

    // --- helpers ---

    private MacroDefinition deserialize(String json) {
        return GSON.fromJson(json, MacroDefinition.class);
    }
}

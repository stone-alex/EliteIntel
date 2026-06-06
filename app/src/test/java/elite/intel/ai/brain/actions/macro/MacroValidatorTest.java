package elite.intel.ai.brain.actions.macro;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MacroValidatorTest {

    @Test
    void validMacroHasNoErrors() {
        MacroDefinition macro = macro("macro_valid", "Valid", "valid phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertTrue(MacroValidator.validate(macro, List.of(), null).isEmpty());
    }

    @Test
    void rejectsUnsafeIdAndBuiltInCommandId() {
        MacroDefinition unsafe = macro("bad id", "Bad", "unique phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));
        MacroDefinition builtIn = macro("deploy_landing_gear", "Bad", "another unique phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroValidator.validate(unsafe, List.of(), null).isEmpty());
        assertFalse(MacroValidator.validate(builtIn, List.of(), null).isEmpty());
    }

    @Test
    void rejectsDuplicateMacroIdExceptOriginalId() {
        MacroDefinition existing = macro("macro_same", "Existing", "existing phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));
        MacroDefinition candidate = macro("macro_same", "Candidate", "candidate phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroValidator.validate(candidate, List.of(existing), null).isEmpty());
        assertTrue(MacroValidator.validate(candidate, List.of(existing), "macro_same").isEmpty());
    }

    @Test
    void rejectsDuplicateMacroPhrase() {
        MacroDefinition existing = macro("macro_existing", "Existing", "duplicate phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));
        MacroDefinition candidate = macro("macro_candidate", "Candidate", "duplicate phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroValidator.validate(candidate, List.of(existing), null).isEmpty());
    }

    @Test
    void rejectsInvalidStepsAndMacroRunCommandTarget() {
        MacroDefinition existing = macro("macro_other", "Other", "other phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));
        MacroDefinition candidate = macro("macro_candidate", "Candidate", "candidate phrase", List.of(
                new MacroStep(MacroStep.Type.DELAY, null, 0, null, null),
                new MacroStep(MacroStep.Type.BINDING_HOLD, "Binding", 0, null, null),
                new MacroStep(MacroStep.Type.RUN_COMMAND, null, 0, null, "macro_other")
        ));

        assertFalse(MacroValidator.validate(candidate, List.of(existing), null).isEmpty());
    }

    // --- parameter validation ---

    @Test
    void validMacroWithParametersHasNoErrors() {
        MacroDefinition macro = macroWithParams("macro_paramtest", "Param Test", "param test phrase",
                List.of(new MacroParameterSpec("lat", "number", true, "latitude", null, null),
                        new MacroParameterSpec("lon", "number", true, "longitude", null, null)),
                List.of(MacroStep.runCommandWithParams("navigate_to_coordinates",
                        Map.of("lat", "${lat}", "lon", "${lon}"))));

        assertTrue(MacroValidator.validate(macro, List.of(), null).isEmpty());
    }

    @Test
    void rejectsDuplicateParameterName() {
        MacroDefinition macro = macroWithParams("macro_dup", "Dup", "dup phrase",
                List.of(new MacroParameterSpec("speed", "string", true, "", null, null),
                        new MacroParameterSpec("speed", "number", false, "", null, null)),
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroValidator.validate(macro, List.of(), null).isEmpty());
    }

    @Test
    void rejectsInvalidParameterType() {
        MacroDefinition macro = macroWithParams("macro_badtype", "Bad Type", "bad type phrase",
                List.of(new MacroParameterSpec("val", "integer", true, "", null, null)),
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroValidator.validate(macro, List.of(), null).isEmpty());
    }

    @Test
    void rejectsInvalidParameterName() {
        MacroDefinition macro = macroWithParams("macro_badname", "Bad Name", "bad name phrase",
                List.of(new MacroParameterSpec("bad name!", "string", true, "", null, null)),
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroValidator.validate(macro, List.of(), null).isEmpty());
    }

    @Test
    void rejectsUndeclaredParamRefInStepParams() {
        // Step references ${lat} but no parameter named "lat" is declared.
        MacroDefinition macro = macroWithParams("macro_undeclared", "Undeclared", "undeclared phrase",
                List.of(new MacroParameterSpec("lon", "number", true, "", null, null)),
                List.of(MacroStep.runCommandWithParams("navigate_to_coordinates",
                        Map.of("lat", "${lat}", "lon", "${lon}"))));

        List<String> errors = MacroValidator.validate(macro, List.of(), null);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("lat")));
    }

    @Test
    void allowsDeclaredButUnusedParam() {
        // "future_param" is declared but not referenced in any step.
        // This must be allowed (reserved for future conditions/repeat).
        MacroDefinition macro = macroWithParams("macro_unused", "Unused Param", "unused param phrase",
                List.of(new MacroParameterSpec("future_param", "boolean", false, "", null, null)),
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertTrue(MacroValidator.validate(macro, List.of(), null).isEmpty());
    }

    @Test
    void rejectsUndeclaredParamRefInSpeakText() {
        MacroDefinition macro = macroWithParams("macro_speakref", "Speak Ref", "speak ref phrase",
                List.of(new MacroParameterSpec("name", "string", true, "", null, null)),
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "Hello ${name}, going to ${dest}", null)));

        List<String> errors = MacroValidator.validate(macro, List.of(), null);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("dest")));
        // "name" is declared, so no error for it
        assertFalse(errors.stream().anyMatch(e -> e.contains("'name'")));
    }

    // --- actionKey format and length validation ---

    @Test
    void rejectsActionKeyWithUppercaseLetters() {
        MacroDefinition macro = macro("Apply_Combat_Preset", "Test", "test phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        List<String> errors = MacroValidator.validate(macro, List.of(), null);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("lowercase")));
    }

    @Test
    void rejectsActionKeyWithHyphen() {
        MacroDefinition macro = macro("apply-combat-preset", "Test", "test phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        List<String> errors = MacroValidator.validate(macro, List.of(), null);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("lowercase")));
    }

    @Test
    void rejectsActionKeyWithDotsAndColons() {
        MacroDefinition macro = macro("apply.combat:preset", "Test", "test phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        List<String> errors = MacroValidator.validate(macro, List.of(), null);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("lowercase")));
    }

    @Test
    void rejectsActionKeyTooShort() {
        MacroDefinition macro = macro("go", "Go", "go phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        List<String> errors = MacroValidator.validate(macro, List.of(), null);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("at least")));
    }

    @Test
    void rejectsActionKeyTooLong() {
        String longKey = "a".repeat(MacroValidator.MAX_ACTION_KEY_LENGTH + 1);
        MacroDefinition macro = macro(longKey, "Test", "test phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        List<String> errors = MacroValidator.validate(macro, List.of(), null);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("must not exceed")));
    }

    @Test
    void acceptsActionKeyAtMinimumLength() {
        // "macro_test" is exactly MIN_ACTION_KEY_LENGTH (10) characters.
        MacroDefinition macro = macro("macro_test", "Test", "test phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertTrue(MacroValidator.validate(macro, List.of(), null).isEmpty());
    }

    @Test
    void patternErrorSuppressesLengthError() {
        // "bad key!" is 8 chars (below minimum) AND contains invalid characters.
        // Only the pattern error must be reported — not the length error.
        MacroDefinition macro = macro("bad key!", "Bad", "bad phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        List<String> errors = MacroValidator.validate(macro, List.of(), null);
        assertTrue(errors.stream().anyMatch(e -> e.contains("lowercase")));
        assertFalse(errors.stream().anyMatch(e -> e.contains("at least")));
    }

    // --- helpers ---

    private static MacroDefinition macro(String id, String name, String phrases, List<MacroStep> steps) {
        return new MacroDefinition(id, name, "", phrases, steps);
    }

    private static MacroDefinition macroWithParams(String id, String name, String phrases,
                                                   List<MacroParameterSpec> params, List<MacroStep> steps) {
        return new MacroDefinition(id, name, "", phrases, params, steps);
    }
}

package elite.intel.ai.brain.actions.macro;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacroEditorValidatorTest {

    @Test
    void validMacroHasNoErrors() {
        MacroDefinition macro = macro("macro_valid", "Valid", "valid phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertTrue(MacroEditorValidator.validate(macro, List.of(), null).isEmpty());
    }

    @Test
    void rejectsUnsafeIdAndBuiltInCommandId() {
        MacroDefinition unsafe = macro("bad id", "Bad", "unique phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));
        MacroDefinition builtIn = macro("deploy_landing_gear", "Bad", "another unique phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroEditorValidator.validate(unsafe, List.of(), null).isEmpty());
        assertFalse(MacroEditorValidator.validate(builtIn, List.of(), null).isEmpty());
    }

    @Test
    void rejectsDuplicateMacroIdExceptOriginalId() {
        MacroDefinition existing = macro("macro_same", "Existing", "existing phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));
        MacroDefinition candidate = macro("macro_same", "Candidate", "candidate phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroEditorValidator.validate(candidate, List.of(existing), null).isEmpty());
        assertTrue(MacroEditorValidator.validate(candidate, List.of(existing), "macro_same").isEmpty());
    }

    @Test
    void rejectsDuplicateMacroPhrase() {
        MacroDefinition existing = macro("macro_existing", "Existing", "duplicate phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));
        MacroDefinition candidate = macro("macro_candidate", "Candidate", "duplicate phrase",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hello", null)));

        assertFalse(MacroEditorValidator.validate(candidate, List.of(existing), null).isEmpty());
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

        assertFalse(MacroEditorValidator.validate(candidate, List.of(existing), null).isEmpty());
    }

    private static MacroDefinition macro(String id, String name, String phrases, List<MacroStep> steps) {
        return new MacroDefinition(id, name, "", phrases, steps);
    }
}

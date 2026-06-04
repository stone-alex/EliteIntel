package elite.intel.ai.brain.actions.macro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MacroRepositoryTest {

    @TempDir
    Path tempDir;

    private final MacroRepository repo = new MacroRepository();

    private Path macrosFile() {
        return tempDir.resolve("macros.json");
    }

    // --- missing / empty file ---

    @Test
    void missingFileReturnsEmptyList() {
        List<MacroDefinition> result = repo.load(macrosFile());
        assertTrue(result.isEmpty());
    }

    @Test
    void emptyFileReturnsEmptyList() throws IOException {
        Files.writeString(macrosFile(), "", StandardCharsets.UTF_8);
        assertTrue(repo.load(macrosFile()).isEmpty());
    }

    @Test
    void blankWhitespaceFileReturnsEmptyList() throws IOException {
        Files.writeString(macrosFile(), "   \n  ", StandardCharsets.UTF_8);
        assertTrue(repo.load(macrosFile()).isEmpty());
    }

    // --- malformed JSON ---

    @Test
    void malformedJsonReturnsEmptyListWithoutException() throws IOException {
        Files.writeString(macrosFile(), "{not valid json at all", StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> repo.load(macrosFile()));
        assertTrue(repo.load(macrosFile()).isEmpty());
    }

    @Test
    void truncatedJsonReturnsEmptyListWithoutException() throws IOException {
        Files.writeString(macrosFile(), "[{\"id\":\"x\",\"name\":\"", StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> repo.load(macrosFile()));
    }

    @Test
    void jsonObjectInsteadOfArrayReturnsEmptyList() throws IOException {
        Files.writeString(macrosFile(), "{\"id\":\"x\"}", StandardCharsets.UTF_8);
        assertTrue(repo.load(macrosFile()).isEmpty());
    }

    // --- invalid macro entries skipped ---

    @Test
    void macroWithBlankIdIsSkipped() throws IOException {
        writeJson("""
                [
                  {"id":"","name":"Bad","phrases":"p","steps":[{"type":"SPEAK","text":"x"}]},
                  {"id":"macro_good","name":"Good","phrases":"good","steps":[{"type":"SPEAK","text":"ok"}]}
                ]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
        assertEquals("macro_good", result.getFirst().getId());
    }

    @Test
    void macroWithBlankNameIsSkipped() throws IOException {
        writeJson("""
                [
                  {"id":"macro_bad","name":"","phrases":"p","steps":[{"type":"SPEAK","text":"x"}]},
                  {"id":"macro_ok","name":"OK","phrases":"ok","steps":[{"type":"SPEAK","text":"ok"}]}
                ]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
        assertEquals("macro_ok", result.getFirst().getId());
    }

    @Test
    void macroWithBlankPhrasesIsSkipped() throws IOException {
        writeJson("""
                [
                  {"id":"macro_bad","name":"Bad","phrases":"","steps":[{"type":"SPEAK","text":"x"}]},
                  {"id":"macro_ok","name":"OK","phrases":"ok phrase","steps":[{"type":"SPEAK","text":"ok"}]}
                ]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
    }

    @Test
    void macroWithEmptyStepsIsSkipped() throws IOException {
        writeJson("""
                [
                  {"id":"macro_bad","name":"Bad","phrases":"p","steps":[]},
                  {"id":"macro_ok","name":"OK","phrases":"ok","steps":[{"type":"SPEAK","text":"ok"}]}
                ]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
    }

    @Test
    void bindingTapStepWithoutBindingIdIsSkipped() throws IOException {
        writeJson("""
                [
                  {"id":"macro_bad","name":"Bad","phrases":"p","steps":[{"type":"BINDING_TAP"}]},
                  {"id":"macro_ok","name":"OK","phrases":"ok","steps":[{"type":"SPEAK","text":"x"}]}
                ]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
        assertEquals("macro_ok", result.getFirst().getId());
    }

    // --- valid macro loaded correctly ---

    @Test
    void validSingleMacroIsLoaded() throws IOException {
        writeJson("""
                [
                  {
                    "id": "macro_test",
                    "name": "Test Macro",
                    "description": "A test",
                    "phrases": "test, run test",
                    "steps": [
                      {"type": "BINDING_TAP", "bindingId": "MyBinding"},
                      {"type": "DELAY", "durationMs": 200},
                      {"type": "SPEAK", "text": "Done"}
                    ]
                  }
                ]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
        MacroDefinition m = result.getFirst();
        assertEquals("macro_test", m.getId());
        assertEquals("Test Macro", m.getName());
        assertEquals("A test", m.getDescription());
        assertEquals("test, run test", m.getPhrases());
        assertEquals(3, m.getSteps().size());
        assertEquals(MacroStep.Type.BINDING_TAP, m.getSteps().get(0).getType());
        assertEquals("MyBinding", m.getSteps().get(0).getBindingId());
        assertEquals(MacroStep.Type.DELAY, m.getSteps().get(1).getType());
        assertEquals(200, m.getSteps().get(1).getDurationMs());
        assertEquals(MacroStep.Type.SPEAK, m.getSteps().get(2).getType());
        assertEquals("Done", m.getSteps().get(2).getText());
    }

    @Test
    void resultIsUnmodifiable() throws IOException {
        writeJson("""
                [{"id":"macro_x","name":"X","phrases":"p","steps":[{"type":"SPEAK","text":"ok"}]}]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertThrows(UnsupportedOperationException.class, () -> result.add(null));
    }

    // --- save + load roundtrip ---

    @Test
    void saveAndLoadRoundtripPreservesAllFields() {
        List<MacroDefinition> original = repo.load(jsonWithOneMacro());

        repo.save(original, macrosFile());
        List<MacroDefinition> reloaded = repo.load(macrosFile());

        assertEquals(1, reloaded.size());
        MacroDefinition m = reloaded.getFirst();
        assertEquals("macro_roundtrip", m.getId());
        assertEquals("Roundtrip", m.getName());
        assertEquals("desc", m.getDescription());
        assertEquals("do roundtrip", m.getPhrases());
        assertEquals(1, m.getSteps().size());
        assertEquals(MacroStep.Type.SPEAK, m.getSteps().getFirst().getType());
        assertEquals("hi", m.getSteps().getFirst().getText());
    }

    // --- helpers ---

    private void writeJson(String json) throws IOException {
        Files.writeString(macrosFile(), json, StandardCharsets.UTF_8);
    }

    private Path jsonWithOneMacro() {
        try {
            writeJson("""
                    [{"id":"macro_roundtrip","name":"Roundtrip","description":"desc",
                      "phrases":"do roundtrip","steps":[{"type":"SPEAK","text":"hi"}]}]
                    """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return macrosFile();
    }
}

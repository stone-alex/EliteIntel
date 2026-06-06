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
                  {"id":"","actionKey":"macro_bad_id","name":"Bad","phrases":"p","steps":[{"type":"SPEAK","text":"x"}]},
                  {"id":"macro_good","actionKey":"macro_good","name":"Good","phrases":"good","steps":[{"type":"SPEAK","text":"ok"}]}
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
                  {"id":"macro_bad","actionKey":"macro_bad_name","name":"","phrases":"p","steps":[{"type":"SPEAK","text":"x"}]},
                  {"id":"macro_ok","actionKey":"macro_ok_step","name":"OK","phrases":"ok","steps":[{"type":"SPEAK","text":"ok"}]}
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
                  {"id":"macro_bad","actionKey":"macro_bad_phrases","name":"Bad","phrases":"","steps":[{"type":"SPEAK","text":"x"}]},
                  {"id":"macro_ok","actionKey":"macro_ok_step","name":"OK","phrases":"ok phrase","steps":[{"type":"SPEAK","text":"ok"}]}
                ]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
    }

    @Test
    void macroWithEmptyStepsIsSkipped() throws IOException {
        writeJson("""
                [
                  {"id":"macro_bad","actionKey":"macro_bad_steps","name":"Bad","phrases":"p","steps":[]},
                  {"id":"macro_ok","actionKey":"macro_ok_step","name":"OK","phrases":"ok","steps":[{"type":"SPEAK","text":"ok"}]}
                ]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
    }

    @Test
    void bindingTapStepWithoutBindingIdIsSkipped() throws IOException {
        writeJson("""
                [
                  {"id":"macro_bad","actionKey":"macro_bad_binding","name":"Bad","phrases":"p","steps":[{"type":"BINDING_TAP"}]},
                  {"id":"macro_ok","actionKey":"macro_ok_step","name":"OK","phrases":"ok","steps":[{"type":"SPEAK","text":"x"}]}
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
                    "actionKey": "macro_test",
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
                [{"id":"macro_x","actionKey":"macro_x_item","name":"X","phrases":"p","steps":[{"type":"SPEAK","text":"ok"}]}]
                """);
        List<MacroDefinition> result = repo.load(macrosFile());
        assertThrows(UnsupportedOperationException.class, () -> result.add(null));
    }

    // --- save + load roundtrip ---

    @Test
    void saveAndLoadRoundtripPreservesAllFields() {
        List<MacroDefinition> original = repo.load(jsonWithOneMacro());

        assertTrue(repo.save(original, macrosFile()));
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

    @Test
    void saveCreatesParentDirectories() {
        Path nestedFile = tempDir.resolve("nested").resolve("macros").resolve("macros.json");
        MacroDefinition macro = new MacroDefinition(
                "macro_nested_save",
                "Nested Save",
                "",
                "nested save",
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "hi", null))
        );

        assertTrue(repo.save(List.of(macro), nestedFile));

        assertTrue(Files.exists(nestedFile));
        assertEquals(1, repo.load(nestedFile).size());
    }

    // --- backup fallback on corrupt main file ---

    @Test
    void loadFallsBackToBackupWhenMainFileIsCorrupt() throws IOException {
        Files.writeString(macrosFile(), "not-valid-json", StandardCharsets.UTF_8);
        Files.writeString(backupFile(), validOneMacroJson("macro_backup", "From Backup"),
                StandardCharsets.UTF_8);

        List<MacroDefinition> result = repo.load(macrosFile());

        assertEquals(1, result.size());
        assertEquals("macro_backup", result.getFirst().getId());
    }

    @Test
    void loadReturnsEmptyWhenMainIsCorruptAndNoBackupExists() throws IOException {
        writeJson("""
                [
                  {"id":"macro_bad","actionKey":"macro_bad_reset","name":"","phrases":"p","steps":[{"type":"SPEAK","text":"x"}]}
                ]
                """);
        repo.load(macrosFile());
        assertEquals(1, repo.getLastSkippedCount());

        Files.writeString(macrosFile(), "not-valid-json", StandardCharsets.UTF_8);

        List<MacroDefinition> result = repo.load(macrosFile());

        assertTrue(result.isEmpty());
        assertEquals(0, repo.getLastSkippedCount());
        assertTrue(repo.getLastSkippedLabels().isEmpty());
    }

    @Test
    void loadReturnsEmptyWhenBothMainAndBackupAreCorrupt() throws IOException {
        Files.writeString(macrosFile(), "not-valid-json", StandardCharsets.UTF_8);
        Files.writeString(backupFile(), "also-not-valid", StandardCharsets.UTF_8);

        List<MacroDefinition> result = repo.load(macrosFile());

        assertTrue(result.isEmpty());
    }

    @Test
    void loadDoesNotCheckBackupWhenMainFileIsMissing() throws IOException {
        Files.writeString(backupFile(), validOneMacroJson("macro_bak_item", "Bak Only"),
                StandardCharsets.UTF_8);

        // Main file does not exist, so backup should not be consulted.
        List<MacroDefinition> result = repo.load(macrosFile());

        assertTrue(result.isEmpty());
    }

    // --- safe save: backup + temp + atomic rename ---

    @Test
    void saveCreatesBackupOfPreviousFile() throws IOException {
        repo.save(List.of(makeMacro("macro_first", "First")), macrosFile());
        repo.save(List.of(makeMacro("macro_second", "Second")), macrosFile());

        assertTrue(Files.exists(backupFile()), "Backup file should exist after second save");
        List<MacroDefinition> fromBackup = repo.load(backupFile());
        assertEquals(1, fromBackup.size());
        assertEquals("macro_first", fromBackup.getFirst().getId());
    }

    @Test
    void saveDoesNotCreateBackupOnFirstSave() {
        assertFalse(Files.exists(backupFile()), "No backup before first save");
        repo.save(List.of(makeMacro("macro_init", "Init")), macrosFile());
        assertFalse(Files.exists(backupFile()), "No backup created when there was no previous file");
    }

    @Test
    void saveTempFileIsRemovedAfterSuccessfulSave() {
        repo.save(List.of(makeMacro("macro_tmp_file", "Temp")), macrosFile());

        Path tmp = macrosFile().resolveSibling("macros.json.tmp");
        assertFalse(Files.exists(tmp), "Temp file should not remain after a successful save");
    }

    @Test
    void savedFileIsReadableAfterSave() {
        MacroDefinition macro = makeMacro("macro_persisted", "Persisted");
        repo.save(List.of(macro), macrosFile());

        List<MacroDefinition> result = repo.load(macrosFile());
        assertEquals(1, result.size());
        assertEquals("macro_persisted", result.getFirst().getId());
    }

    // --- helpers ---

    private void writeJson(String json) throws IOException {
        Files.writeString(macrosFile(), json, StandardCharsets.UTF_8);
    }

    private Path backupFile() {
        return macrosFile().resolveSibling("macros.json.bak");
    }

    private Path jsonWithOneMacro() {
        try {
            writeJson("""
                    [{"id":"macro_roundtrip","actionKey":"macro_roundtrip","name":"Roundtrip","description":"desc",
                      "phrases":"do roundtrip","steps":[{"type":"SPEAK","text":"hi"}]}]
                    """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return macrosFile();
    }

    /** Produces a minimal valid single-macro JSON array with the given {@code id} used as actionKey. */
    private static String validOneMacroJson(String id, String name) {
        return "[{\"id\":\"" + id + "\",\"actionKey\":\"" + id + "\","
                + "\"name\":\"" + name + "\","
                + "\"phrases\":\"trigger\",\"steps\":[{\"type\":\"SPEAK\",\"text\":\"ok\"}]}]";
    }

    private static MacroDefinition makeMacro(String id, String name) {
        return new MacroDefinition(id, name, "", "trigger " + id,
                List.of(new MacroStep(MacroStep.Type.SPEAK, null, 0, "ok", null)));
    }
}

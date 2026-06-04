package elite.intel.ai.brain.actions.macro;

import com.google.gson.Gson;
import elite.intel.ai.brain.Reducer;
import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MacroRegistryTest {

    private static final Gson GSON = new Gson();
    private final MacroRegistry registry = MacroRegistry.getInstance();

    @BeforeEach
    void resetRegistry() {
        registry.setMacros(List.of());
    }

    @AfterEach
    void restoreRegistry() {
        registry.setMacros(List.of());
    }

    // --- getMacros() ---

    @Test
    void getMacrosReturnsEmptyListAfterReset() {
        assertTrue(registry.getMacros().isEmpty());
    }

    @Test
    void setMacrosReplacesExistingList() {
        MacroDefinition macro = buildMacro("macro_a", "alpha");
        registry.setMacros(List.of(macro));
        assertEquals(1, registry.getMacros().size());
        assertEquals("macro_a", registry.getMacros().getFirst().getId());

        registry.setMacros(List.of());
        assertTrue(registry.getMacros().isEmpty());
    }

    @Test
    void replaceMacrosUpdatesPublicSnapshot() {
        registry.replaceMacros(List.of(buildMacro("macro_replace", "replace phrase")));

        assertEquals(1, registry.getMacros().size());
        assertEquals("macro_replace", registry.getMacros().getFirst().getId());
    }

    @Test
    void getMacrosListIsUnmodifiable() {
        registry.setMacros(List.of(buildMacro("macro_a", "alpha")));
        assertThrows(UnsupportedOperationException.class,
                () -> registry.getMacros().add(null));
    }

    // --- contributeToActionMap() ---

    @Test
    void contributeToActionMapAddsAliasEntryForEachMacroPhrase() {
        registry.setMacros(List.of(
                buildMacro("macro_a", "do alpha, alpha command"),
                buildMacro("macro_b", "do beta")
        ));
        Map<String, String> map = new LinkedHashMap<>();
        registry.contributeToActionMap(map);

        assertEquals("macro_a", map.get("do alpha"));
        assertEquals("macro_a", map.get("alpha command"));
        assertEquals("macro_b", map.get("do beta"));
        assertEquals(3, map.size());
    }

    @Test
    void contributeToActionMapDoesNothingWhenEmpty() {
        Map<String, String> map = new LinkedHashMap<>();
        registry.contributeToActionMap(map);
        assertTrue(map.isEmpty());
    }

    @Test
    void contributeToActionMapDoesNotClearExistingEntries() {
        registry.setMacros(List.of(buildMacro("macro_x", "trigger x")));
        Map<String, String> map = new LinkedHashMap<>();
        map.put("existing_phrase", "existing_action");
        registry.contributeToActionMap(map);

        assertEquals("existing_action", map.get("existing_phrase"));
        assertEquals("macro_x", map.get("trigger x"));
    }

    @Test
    void contributeToActionMapDoesNotOverrideExistingPhrase() {
        registry.setMacros(List.of(buildMacro("macro_x", "existing_phrase, new phrase")));
        Map<String, String> map = new LinkedHashMap<>();
        map.put("existing_phrase", "existing_action");

        registry.contributeToActionMap(map);

        assertEquals("existing_action", map.get("existing_phrase"));
        assertEquals("macro_x", map.get("new phrase"));
        assertEquals(2, map.size());
    }

    @Test
    void reducerKeepsExactMacroPhraseAsHighConfidenceCandidate() {
        registry.setMacros(List.of(buildMacro("macro_exact", "example macro, macro status check")));
        Map<String, String> map = new LinkedHashMap<>();
        registry.contributeToActionMap(map);

        Map<String, String> reduced = Reducer.reduce("macro status check", map, false);

        assertEquals("macro_exact", reduced.get("macro status check"));
    }

    @Test
    void contributeToActionMapNormalizesMacroPhraseCaseForSttInput() {
        registry.setMacros(List.of(buildMacro("macro_case", "Example Macro")));
        Map<String, String> map = new LinkedHashMap<>();

        registry.contributeToActionMap(map);

        assertEquals("macro_case", map.get("example macro"));
    }

    // --- contributeToHandlerMap() ---

    @Test
    void contributeToHandlerMapAddsMacroCommandHandlerPerMacro() {
        registry.setMacros(List.of(
                buildMacro("macro_a", "phrase_a"),
                buildMacro("macro_b", "phrase_b")
        ));
        Map<String, CommandHandler> handlers = new HashMap<>();
        registry.contributeToHandlerMap(handlers);

        assertTrue(handlers.containsKey("macro_a"));
        assertTrue(handlers.containsKey("macro_b"));
        assertInstanceOf(MacroCommandHandler.class, handlers.get("macro_a"));
        assertInstanceOf(MacroCommandHandler.class, handlers.get("macro_b"));
    }

    @Test
    void contributeToHandlerMapDoesNothingWhenEmpty() {
        Map<String, CommandHandler> handlers = new HashMap<>();
        registry.contributeToHandlerMap(handlers);
        assertTrue(handlers.isEmpty());
    }

    @Test
    void contributeToHandlerMapDoesNotOverrideExistingHandler() {
        registry.setMacros(List.of(buildMacro("existing_action", "phrase")));
        CommandHandler existingHandler = (action, params, responseText) -> {};
        Map<String, CommandHandler> handlers = new HashMap<>();
        handlers.put("existing_action", existingHandler);

        registry.contributeToHandlerMap(handlers);

        assertSame(existingHandler, handlers.get("existing_action"));
        assertEquals(1, handlers.size());
    }

    @Test
    void duplicateMacroIdsLastOneWins() {
        MacroDefinition first = buildMacro("macro_dup", "phrase_first");
        MacroDefinition second = buildMacro("macro_dup", "phrase_second");
        registry.setMacros(List.of(first, second));

        Map<String, CommandHandler> handlers = new HashMap<>();
        registry.contributeToHandlerMap(handlers);

        // Both were put; the map has one entry (last write wins in HashMap)
        assertEquals(1, handlers.size());
        assertTrue(handlers.containsKey("macro_dup"));
    }

    @Test
    void setNullMacrosTreatedAsEmpty() {
        registry.setMacros(null);
        assertTrue(registry.getMacros().isEmpty());
    }

    // --- helpers ---

    private MacroDefinition buildMacro(String id, String phrases) {
        return GSON.fromJson(
                "{\"id\":\"" + id + "\",\"name\":\"" + id + "\",\"phrases\":\"" + phrases + "\"," +
                "\"steps\":[{\"type\":\"SPEAK\",\"text\":\"ok\"}]}",
                MacroDefinition.class);
    }
}

package elite.intel.ai.brain.actions.catalog;

import com.google.gson.Gson;
import elite.intel.ai.brain.actions.Commands;
import elite.intel.ai.brain.actions.handlers.commands.SimpleCommandActionHandler;
import elite.intel.ai.brain.actions.macro.MacroDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CommandCatalogTest {

    private final CommandCatalog catalog = new CommandCatalog(Function.identity());

    @Test
    void containsOneEntryForEveryCommandsEnumEntry() {
        Map<String, Long> entryCountsById = catalog.entries().stream()
                .collect(Collectors.groupingBy(CommandCatalogEntry::id, Collectors.counting()));
        Map<String, Long> commandCountsById = List.of(Commands.values()).stream()
                .collect(Collectors.groupingBy(this::idOf, Collectors.counting()));

        assertEquals(Commands.values().length, catalog.entries().size());
        assertEquals(commandCountsById, entryCountsById);
    }

    @Test
    void everyEntryIdIsNonBlank() {
        for (CommandCatalogEntry entry : catalog.entries()) {
            assertFalse(entry.id().isBlank());
        }
    }

    @Test
    void entryTypesAreDerivedFromCommandHandlerAndBinding() {
        assertCatalogMatchesCommands((command, entry) ->
                assertEquals(expectedType(command), entry.type(), command.name())
        );
    }

    @Test
    void missingLocalizationKeysFallBackToNonBlankNameAndDescription() {
        assertCatalogMatchesCommands((command, entry) -> {
            assertFalse(entry.name().isBlank(), command.name());
            assertFalse(entry.description().isBlank(), command.name());
            assertFalse(localizationKey(entry.id(), "name").equals(entry.name()), command.name());
            assertFalse(localizationKey(entry.id(), "description").equals(entry.description()), command.name());
        });
    }

    private void assertCatalogMatchesCommands(CommandEntryAssertion assertion) {
        Commands[] commands = Commands.values();
        List<CommandCatalogEntry> entries = catalog.entries();

        assertEquals(commands.length, entries.size());
        for (int i = 0; i < commands.length; i++) {
            assertion.accept(commands[i], entries.get(i));
        }
    }

    private CommandCatalogEntryType expectedType(Commands command) {
        if (command.getHandlerClass() == SimpleCommandActionHandler.class && command.getBinding() != null) {
            return CommandCatalogEntryType.BUILT_IN_BINDING;
        }
        return CommandCatalogEntryType.BUILT_IN_ACTION;
    }

    private String idOf(Commands command) {
        String action = command.getAction();
        if (action != null && !action.isBlank()) {
            return action;
        }
        return command.name().toLowerCase(Locale.ROOT);
    }

    private String localizationKey(String id, String field) {
        return "command." + id + "." + field;
    }

    @FunctionalInterface
    private interface CommandEntryAssertion {
        void accept(Commands command, CommandCatalogEntry entry);
    }

    // ---- entries(List<MacroDefinition>) overload ----

    @Test
    void builtInEntriesStillPresentWhenMacroListIsEmpty() {
        List<CommandCatalogEntry> entries = catalog.entries(List.of());
        assertEquals(Commands.values().length, entries.size());
    }

    @Test
    void macroEntriesAppendedAfterBuiltIns() {
        MacroDefinition macro = buildMacro("macro_test", "Test Macro", "desc");
        List<CommandCatalogEntry> entries = catalog.entries(List.of(macro));

        assertEquals(Commands.values().length + 1, entries.size());
        CommandCatalogEntry macroEntry = entries.getLast();
        assertEquals("macro_test", macroEntry.id());
        assertEquals("Test Macro", macroEntry.name());
    }

    @Test
    void macroEntryTypeIsUserMacroAndIsMacroReturnsTrue() {
        MacroDefinition macro = buildMacro("macro_x", "X", "desc");
        CommandCatalogEntry entry = catalog.entries(List.of(macro)).getLast();

        assertEquals(CommandCatalogEntryType.USER_MACRO, entry.type());
        assertTrue(entry.isMacro());
    }

    @Test
    void builtInEntriesHaveIsMacroFalse() {
        for (CommandCatalogEntry entry : catalog.entries()) {
            assertFalse(entry.isMacro(), "Built-in entry " + entry.id() + " must not be a macro");
        }
    }

    @Test
    void blankMacroDescriptionFallsBackToDefaultText() {
        MacroDefinition macro = buildMacro("macro_nodesc", "My Macro", "");
        CommandCatalogEntry entry = catalog.entries(List.of(macro)).getLast();

        assertEquals("User macro: My Macro", entry.description());
    }

    @Test
    void nonBlankMacroDescriptionIsPreserved() {
        MacroDefinition macro = buildMacro("macro_desc", "My Macro", "Custom description");
        CommandCatalogEntry entry = catalog.entries(List.of(macro)).getLast();

        assertEquals("Custom description", entry.description());
    }

    @Test
    void distinctBindingIdsDeduplicatesAndExcludesNonBindingSteps() {
        MacroDefinition macro = buildMacro("macro_bindings", "Bindings", "desc",
                "[{\"type\":\"BINDING_TAP\",\"bindingId\":\"A\"}," +
                "{\"type\":\"BINDING_HOLD\",\"bindingId\":\"B\",\"durationMs\":200}," +
                "{\"type\":\"DELAY\",\"durationMs\":100}," +
                "{\"type\":\"SPEAK\",\"text\":\"hi\"}," +
                "{\"type\":\"BINDING_TAP\",\"bindingId\":\"A\"}]");

        List<String> ids = macro.distinctBindingIds();
        assertEquals(List.of("A", "B"), ids);
    }

    @Test
    void distinctBindingIdsEmptyWhenOnlyDelayAndSpeak() {
        MacroDefinition macro = buildMacro("macro_nodeps", "No Deps", "",
                "[{\"type\":\"DELAY\",\"durationMs\":0},{\"type\":\"SPEAK\",\"text\":\"x\"}]");

        assertTrue(macro.distinctBindingIds().isEmpty());
    }

    // ---- helpers for macro tests ----

    private static final Gson MACRO_GSON = new Gson();

    private MacroDefinition buildMacro(String id, String name, String description) {
        return buildMacro(id, name, description,
                "[{\"type\":\"SPEAK\",\"text\":\"ok\"}]");
    }

    private MacroDefinition buildMacro(String id, String name, String description, String stepsJson) {
        String json = "{\"id\":\"" + id + "\",\"name\":\"" + name + "\"," +
                      "\"description\":\"" + description + "\"," +
                      "\"phrases\":\"trigger " + id + "\"," +
                      "\"steps\":" + stepsJson + "}";
        return MACRO_GSON.fromJson(json, MacroDefinition.class);
    }
}

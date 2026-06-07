package elite.intel.ai.brain.actions.catalog;

import com.google.gson.Gson;
import elite.intel.ai.brain.actions.Commands;
import elite.intel.ai.brain.actions.handlers.commands.SimpleCommandActionHandler;
import elite.intel.ai.brain.actions.customcommand.CustomCommandDefinition;
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

    // ---- entries(List<CustomCommandDefinition>) overload ----

    @Test
    void builtInEntriesStillPresentWhenCustomCommandListIsEmpty() {
        List<CommandCatalogEntry> entries = catalog.entries(List.of());
        assertEquals(Commands.values().length, entries.size());
    }

    @Test
    void customCommandEntriesAppendedAfterBuiltIns() {
        CustomCommandDefinition customCommand = buildCustomCommand("custom_command_test", "Test Custom Command", "desc");
        List<CommandCatalogEntry> entries = catalog.entries(List.of(customCommand));

        assertEquals(Commands.values().length + 1, entries.size());
        CommandCatalogEntry customCommandEntry = entries.getLast();
        assertEquals("custom_command_test", customCommandEntry.id());
        assertEquals("Test Custom Command", customCommandEntry.name());
    }

    @Test
    void customCommandEntryTypeIsUserCustomCommandAndIsCustomCommandReturnsTrue() {
        CustomCommandDefinition customCommand = buildCustomCommand("custom_command_x", "X", "desc");
        CommandCatalogEntry entry = catalog.entries(List.of(customCommand)).getLast();

        assertEquals(CommandCatalogEntryType.CUSTOM_COMMAND, entry.type());
        assertTrue(entry.isCustomCommand());
    }

    @Test
    void builtInEntriesHaveIsCustomCommandFalse() {
        for (CommandCatalogEntry entry : catalog.entries()) {
            assertFalse(entry.isCustomCommand(), "Built-in entry " + entry.id() + " must not be a customCommand");
        }
    }

    @Test
    void blankCustomCommandDescriptionFallsBackToDefaultText() {
        CustomCommandDefinition customCommand = buildCustomCommand("custom_command_nodesc", "My Custom Command", "");
        CommandCatalogEntry entry = catalog.entries(List.of(customCommand)).getLast();

        assertEquals("User custom command: My Custom Command", entry.description());
    }

    @Test
    void nonBlankCustomCommandDescriptionIsPreserved() {
        CustomCommandDefinition customCommand = buildCustomCommand("custom_command_desc", "My Custom Command", "Custom description");
        CommandCatalogEntry entry = catalog.entries(List.of(customCommand)).getLast();

        assertEquals("Custom description", entry.description());
    }

    @Test
    void distinctBindingIdsDeduplicatesAndExcludesNonBindingSteps() {
        CustomCommandDefinition customCommand = buildCustomCommand("custom_command_bindings", "Bindings", "desc",
                "[{\"type\":\"BINDING_TAP\",\"bindingId\":\"A\"}," +
                "{\"type\":\"BINDING_HOLD\",\"bindingId\":\"B\",\"durationMs\":200}," +
                "{\"type\":\"DELAY\",\"durationMs\":100}," +
                "{\"type\":\"SPEAK\",\"text\":\"hi\"}," +
                "{\"type\":\"BINDING_TAP\",\"bindingId\":\"A\"}]");

        List<String> ids = customCommand.distinctBindingIds();
        assertEquals(List.of("A", "B"), ids);
    }

    @Test
    void distinctBindingIdsEmptyWhenOnlyDelayAndSpeak() {
        CustomCommandDefinition customCommand = buildCustomCommand("custom_command_nodeps", "No Deps", "",
                "[{\"type\":\"DELAY\",\"durationMs\":0},{\"type\":\"SPEAK\",\"text\":\"x\"}]");

        assertTrue(customCommand.distinctBindingIds().isEmpty());
    }

    // ---- helpers for custom command tests ----

    private static final Gson CUSTOM_COMMAND_GSON = new Gson();

    private CustomCommandDefinition buildCustomCommand(String id, String name, String description) {
        return buildCustomCommand(id, name, description,
                "[{\"type\":\"SPEAK\",\"text\":\"ok\"}]");
    }

    private CustomCommandDefinition buildCustomCommand(String id, String name, String description, String stepsJson) {
        String json = "{\"id\":\"" + id + "\",\"name\":\"" + name + "\"," +
                      "\"description\":\"" + description + "\"," +
                      "\"phrases\":\"trigger " + id + "\"," +
                      "\"steps\":" + stepsJson + "}";
        return CUSTOM_COMMAND_GSON.fromJson(json, CustomCommandDefinition.class);
    }
}

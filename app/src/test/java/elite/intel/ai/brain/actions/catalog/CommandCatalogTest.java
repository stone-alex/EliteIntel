package elite.intel.ai.brain.actions.catalog;

import elite.intel.ai.brain.actions.Commands;
import elite.intel.ai.brain.actions.handlers.commands.SimpleCommandActionHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}

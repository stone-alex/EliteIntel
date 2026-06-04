package elite.intel.ai.brain.actions.catalog;

import elite.intel.ai.brain.actions.Commands;
import elite.intel.ai.brain.actions.handlers.commands.SimpleCommandActionHandler;
import elite.intel.ai.brain.actions.macro.MacroDefinition;
import elite.intel.ui.i18n.MultiLingualTextProvider;
import elite.intel.util.StringUtls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Read-only projection of the built-in command registry.
 * <p>
 * {@link Commands} remains the source of truth: every catalog entry is derived from
 * {@link Commands#values()}, and this class must not maintain a separate hardcoded
 * list of built-in commands. The catalog is metadata only; it does not execute
 * commands and is intentionally not wired into ResponseRouter or CommandHandlerFactory.
 */
public final class CommandCatalog {

    private static final String COMMAND_KEY_PREFIX = "command.";
    private static final String NAME_KEY_SUFFIX = ".name";
    private static final String DESCRIPTION_KEY_SUFFIX = ".description";
    private final Function<String, String> textResolver;

    public CommandCatalog() {
        this(MultiLingualTextProvider::getText);
    }

    /**
     * Test seam for localization fallback behavior. Production code uses
     * {@link MultiLingualTextProvider} through the public constructor.
     */
    CommandCatalog(Function<String, String> textResolver) {
        this.textResolver = Objects.requireNonNull(textResolver, "textResolver");
    }

    public List<CommandCatalogEntry> entries() {
        return Arrays.stream(Commands.values())
                .map(this::entryFrom)
                .toList();
    }

    /**
     * Returns all catalog entries: built-in commands followed by user-defined macros.
     * Built-in entries are derived from {@link Commands#values()} as before; macro entries
     * are built from the provided list. The existing {@link #entries()} method is unchanged.
     */
    public List<CommandCatalogEntry> entries(List<MacroDefinition> macros) {
        Objects.requireNonNull(macros, "macros");
        List<CommandCatalogEntry> all = new ArrayList<>(entries());
        for (MacroDefinition macro : macros) {
            String desc = macro.getDescription().isBlank()
                    ? "User macro: " + macro.getName()
                    : macro.getDescription();
            all.add(new CommandCatalogEntry(macro.getId(), macro.getName(), desc, CommandCatalogEntryType.USER_MACRO));
        }
        return Collections.unmodifiableList(all);
    }

    public Optional<CommandCatalogEntry> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return entries().stream()
                .filter(entry -> entry.id().equalsIgnoreCase(id))
                .findFirst();
    }

    private CommandCatalogEntry entryFrom(Commands command) {
        Objects.requireNonNull(command, "command");
        String id = idOf(command);
        CommandCatalogEntryType type = typeOf(command);
        return new CommandCatalogEntry(
                id,
                localizedName(id, command),
                localizedDescription(id, command),
                type
        );
    }

    private static String idOf(Commands command) {
        String action = command.getAction();
        if (action != null && !action.isBlank()) {
            return action;
        }
        return command.name().toLowerCase(Locale.ROOT);
    }

    private static CommandCatalogEntryType typeOf(Commands command) {
        if (command.getHandlerClass() == SimpleCommandActionHandler.class && command.getBinding() != null) {
            return CommandCatalogEntryType.BUILT_IN_BINDING;
        }
        return CommandCatalogEntryType.BUILT_IN_ACTION;
    }

    private String localizedName(String id, Commands command) {
        String key = COMMAND_KEY_PREFIX + id + NAME_KEY_SUFFIX;
        String localized = textResolver.apply(key);
        if (!key.equals(localized)) {
            return localized;
        }
        return humanize(idOf(command));
    }

    private String localizedDescription(String id, Commands command) {
        String key = COMMAND_KEY_PREFIX + id + DESCRIPTION_KEY_SUFFIX;
        String localized = textResolver.apply(key);
        if (!key.equals(localized)) {
            return localized;
        }
        return "Built-in command action: " + idOf(command);
    }

    private static String humanize(String value) {
        String text = value == null || value.isBlank() ? "Command" : value;
        String humanized = StringUtls.capitalizeWords(text.replace('_', ' '));
        return humanized == null || humanized.isBlank() ? "Command" : humanized;
    }
}

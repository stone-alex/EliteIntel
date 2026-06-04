package elite.intel.ai.brain.actions.catalog;

import java.util.Objects;

/**
 * Immutable catalog row for one command exposed to future UI or tooling.
 * <p>
 * This is descriptive metadata only. Built-in entries are generated from
 * {@code Commands.values()}, while future {@link CommandCatalogEntryType#USER_MACRO}
 * entries can represent user-defined editable macros without changing the built-in
 * command source of truth.
 */
public record CommandCatalogEntry(
        String id,
        String name,
        String description,
        CommandCatalogEntryType type
) {

    public CommandCatalogEntry {
        id = requireText(id, "id");
        name = requireText(name, "name");
        description = requireText(description, "description");
        Objects.requireNonNull(type, "type");
    }

    /** Returns {@code true} when this entry represents a user-defined macro rather than a built-in command. */
    public boolean isMacro() {
        return type == CommandCatalogEntryType.USER_MACRO;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}

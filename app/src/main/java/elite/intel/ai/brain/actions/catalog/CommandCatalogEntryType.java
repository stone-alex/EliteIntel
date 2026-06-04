package elite.intel.ai.brain.actions.catalog;

/**
 * High-level command catalog categories.
 */
public enum CommandCatalogEntryType {
    /**
     * Built-in command handled by SimpleCommandActionHandler with a non-null binding.
     */
    BUILT_IN_BINDING,

    /**
     * Built-in handler-driven action. This is not a user-editable macro.
     */
    BUILT_IN_ACTION,

    /**
     * Reserved for future user-defined editable macros.
     */
    USER_MACRO
}

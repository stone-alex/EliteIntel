package elite.intel.ai.brain.actions.catalog;

import java.util.Objects;

/**
 * Visible input metadata for catalog display.
 * <p>
 * This is not a {@code GameInputStep} and must not be treated as an exact runtime
 * execution sequence. Delays and other non-visible timing details are intentionally
 * not represented as input blocks.
 */
public record CommandCatalogInputBlock(String id, String label, String bindingId) {

    public CommandCatalogInputBlock {
        id = requireText(id, "id");
        label = requireText(label, "label");
        bindingId = requireText(bindingId, "bindingId");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}

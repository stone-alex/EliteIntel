package elite.intel.ai.brain.actions.macro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A user-defined macro: a named sequence of steps triggered by voice phrases.
 * Gson populates fields directly; call {@link #validate()} after deserialization.
 */
public final class MacroDefinition {

    private final String id;
    private final String name;
    private final String description;
    /**
     * Comma-separated trigger phrases in the same format as alias provider keys,
     * e.g. {@code "combat sequence, weapons and target, arms and lock"}.
     * The entire string becomes one key in the LLM action map.
     */
    private final String phrases;
    private final List<MacroStep> steps;

    /**
     * Creates a macro definition for editor-created user macros.
     * The provided step list is defensively copied and exposed as an immutable list.
     */
    public MacroDefinition(String id, String name, String description, String phrases, List<MacroStep> steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.phrases = phrases;
        this.steps = steps == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(steps));
    }

    @SuppressWarnings("unused")
    private MacroDefinition() {
        id = null;
        name = null;
        description = null;
        phrases = null;
        steps = null;
    }

    /** Validates all required fields and delegates to each step's own validation. */
    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Macro id is blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Macro '" + id + "': name is blank");
        }
        if (phrases == null || phrases.isBlank()) {
            throw new IllegalArgumentException("Macro '" + id + "': phrases is blank");
        }
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Macro '" + id + "': steps list is empty");
        }
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i) == null) {
                throw new IllegalArgumentException("Macro '" + id + "': step " + i + " is null");
            }
            steps.get(i).validate(i);
        }
    }

    /**
     * Returns the ordered list of distinct binding IDs used by {@link MacroStep.Type#BINDING_TAP}
     * and {@link MacroStep.Type#BINDING_HOLD} steps. {@code DELAY}, {@code SPEAK}, and
     * {@code RUN_COMMAND} steps are excluded. Duplicate binding IDs appear only once, in
     * first-occurrence order.
     */
    public List<String> distinctBindingIds() {
        if (steps == null) return List.of();
        return steps.stream()
                .filter(s -> s.getType() == MacroStep.Type.BINDING_TAP
                          || s.getType() == MacroStep.Type.BINDING_HOLD)
                .map(MacroStep::getBindingId)
                .filter(bid -> bid != null && !bid.isBlank())
                .distinct()
                .toList();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    /** Returns description or empty string if not set. */
    public String getDescription() { return description != null ? description : ""; }
    public String getPhrases() { return phrases; }
    public List<MacroStep> getSteps() { return steps == null ? List.of() : steps; }
}

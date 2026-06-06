package elite.intel.ai.brain.actions.macro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A user-defined macro: a named sequence of steps triggered by voice phrases.
 * Gson populates fields directly; call {@link #validate()} after deserialization.
 * <p>
 * Identity model:
 * <ul>
 *   <li>{@code id} – immutable UUID generated at creation; used as stable storage identity only.</li>
 *   <li>{@code actionKey} – human-readable routing token used by the LLM, handler map, and logs;
 *       editable via the macro editor.</li>
 * </ul>
 */
public final class MacroDefinition {

    private final String id;
    /** LLM-facing action token; editable, unique, validated by {@link MacroValidator}. */
    private final String actionKey;
    private final String name;
    private final String description;
    /**
     * Comma-separated trigger phrases in the same format as alias provider keys,
     * e.g. {@code "navigate and set speed, go to coordinates at speed"}.
     * Phrases may include parameter placeholders such as {@code {lat:number, lon:number}} as
     * LLM training hints; these are parsed by {@code ActionParameterKeyExtractor} for type inference
     * but are not evaluated or substituted at macro execution time.
     */
    private final String phrases;
    /** Optional parameter contract; may be null for parameterless macros (backward-compatible). */
    private final List<MacroParameterSpec> parameters;
    private final List<MacroStep> steps;

    /**
     * Primary constructor for editor-created macros.
     *
     * @param id        immutable UUID; used only as stable storage identity
     * @param actionKey LLM-facing routing token; editable, must be unique among macros
     */
    public MacroDefinition(String id, String actionKey, String name, String description, String phrases,
                           List<MacroParameterSpec> parameters, List<MacroStep> steps) {
        this.id = id;
        this.actionKey = actionKey;
        this.name = name;
        this.description = description;
        this.phrases = phrases;
        this.parameters = parameters == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(parameters));
        this.steps = steps == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(steps));
    }

    /**
     * Backward-compatible constructor: {@code actionKey} defaults to {@code id}.
     * Prefer the 7-arg constructor for new code.
     */
    public MacroDefinition(String id, String name, String description, String phrases,
                           List<MacroParameterSpec> parameters, List<MacroStep> steps) {
        this(id, id, name, description, phrases, parameters, steps);
    }

    /**
     * Creates a parameterless macro definition. Equivalent to passing {@code null} for parameters.
     */
    public MacroDefinition(String id, String name, String description, String phrases, List<MacroStep> steps) {
        this(id, id, name, description, phrases, null, steps);
    }

    @SuppressWarnings("unused")
    private MacroDefinition() {
        id = null;
        actionKey = null;
        name = null;
        description = null;
        phrases = null;
        parameters = null;
        steps = null;
    }

    /** Validates all required fields and delegates to each parameter's and step's own validation. */
    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Macro id is blank");
        }
        if (actionKey == null || actionKey.isBlank()) {
            throw new IllegalArgumentException("Macro '" + id + "': actionKey is blank");
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
        if (parameters != null) {
            for (MacroParameterSpec param : parameters) {
                if (param == null) {
                    throw new IllegalArgumentException("Macro '" + id + "': parameter entry is null");
                }
                param.validate();
            }
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
    /** Returns the LLM-facing action token used for routing, handler lookup, and prompt output. */
    public String getActionKey() { return actionKey; }
    public String getName() { return name; }
    /** Returns description or empty string if not set. */
    public String getDescription() { return description != null ? description : ""; }
    public String getPhrases() { return phrases; }
    /** Returns the parameter contract. Empty list means this macro has no declared parameters. */
    public List<MacroParameterSpec> getParameters() { return parameters != null ? parameters : List.of(); }
    public List<MacroStep> getSteps() { return steps == null ? List.of() : steps; }
}

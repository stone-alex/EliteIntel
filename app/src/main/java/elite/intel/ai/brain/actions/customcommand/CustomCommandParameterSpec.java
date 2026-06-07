package elite.intel.ai.brain.actions.customcommand;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Declares one named parameter in a customCommand's invocation contract.
 * <p>
 * The LLM uses these specs to determine what JSON params to return when invoking the customCommand.
 * Gson populates fields directly; call {@link #validate()} after deserialization.
 */
public final class CustomCommandParameterSpec {

    public static final Set<String> VALID_TYPES = Set.of("string", "number", "boolean");
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]+");

    private final String name;
    private final String type;
    private final boolean required;
    private final String description;
    private final List<String> examples;
    private final String extractionHint;

    /**
     * @param name            identifier used in {@code ${name}} templates within step params and SPEAK text
     * @param type            one of {@code "string"}, {@code "number"}, or {@code "boolean"}
     * @param required        if {@code true}, the customCommand aborts at runtime when this param is absent
     * @param description     human-readable hint shown in the LLM prompt and UI details view
     * @param examples        sample values shown to the LLM to improve extraction accuracy
     * @param extractionHint  optional extra rule appended to the LLM prompt for this parameter
     *                        (e.g. "use NATO alphabet letters verbatim")
     */
    public CustomCommandParameterSpec(String name, String type, boolean required,
                               String description, List<String> examples, String extractionHint) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.description = description;
        this.examples = examples == null ? List.of() : List.copyOf(examples);
        this.extractionHint = extractionHint;
    }

    @SuppressWarnings("unused")
    private CustomCommandParameterSpec() {
        name = null;
        type = null;
        required = false;
        description = null;
        examples = null;
        extractionHint = null;
    }

    /** Validates this spec. Throws {@link IllegalArgumentException} if invalid. */
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("CustomCommandParameterSpec: name is required");
        }
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "CustomCommandParameterSpec: name '" + name + "' may only contain letters, digits, or underscore");
        }
        if (type == null || !VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                    "CustomCommandParameterSpec: type must be one of " + VALID_TYPES + ", got: " + type);
        }
    }

    public String getName() { return name; }
    public String getType() { return type != null ? type : "string"; }
    public boolean isRequired() { return required; }
    public String getDescription() { return description != null ? description : ""; }
    public List<String> getExamples() { return examples != null ? List.copyOf(examples) : List.of(); }
    /** Returns an optional extra rule appended to the LLM prompt for this parameter, or {@code null} if absent. */
    public String getExtractionHint() { return extractionHint; }
}

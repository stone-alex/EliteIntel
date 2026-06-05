package elite.intel.ai.brain.actions.macro;

/**
 * Thrown when a macro step references a parameter that cannot be resolved from the invocation context.
 * This indicates either a missing optional parameter or a misconfigured step param mapping.
 */
public final class UnresolvedMacroParamException extends RuntimeException {

    private final String paramName;

    public UnresolvedMacroParamException(String paramName, String template) {
        super("Unresolved macro parameter '" + paramName + "' in template: " + template);
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }
}

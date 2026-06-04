package elite.intel.ai.brain.actions.macro;

/**
 * One step in a user-defined macro sequence.
 * Gson populates fields directly; call {@link #validate(int)} after deserialization.
 */
public final class MacroStep {

    public enum Type {
        /** Tap an Elite Dangerous game binding once. Requires {@code bindingId}. */
        BINDING_TAP,
        /** Hold an Elite Dangerous game binding for {@code durationMs} milliseconds. Requires {@code bindingId} and {@code durationMs}. */
        BINDING_HOLD,
        /** Pause execution for {@code durationMs} milliseconds. */
        DELAY,
        /** Speak {@code text} via TTS (publishes {@code AiVoxResponseEvent}). */
        SPEAK,
        /**
         * Delegate to an existing built-in command handler by {@code actionId}.
         * Must reference a Commands action ID, not another macro ID.
         */
        RUN_COMMAND
    }

    private final Type type;
    private final String bindingId;
    private final int durationMs;
    private final String text;
    private final String actionId;

    /**
     * Creates one macro step. Only the fields required by {@code type} are used at execution time.
     */
    public MacroStep(Type type, String bindingId, int durationMs, String text, String actionId) {
        this.type = type;
        this.bindingId = bindingId;
        this.durationMs = durationMs;
        this.text = text;
        this.actionId = actionId;
    }

    @SuppressWarnings("unused")
    private MacroStep() {
        type = null;
        bindingId = null;
        durationMs = 0;
        text = null;
        actionId = null;
    }

    /** Validates required fields for this step's type. */
    public void validate(int stepIndex) {
        if (type == null) {
            throw new IllegalArgumentException("MacroStep[" + stepIndex + "]: type is null");
        }
        switch (type) {
            case BINDING_TAP ->
                require(bindingId != null && !bindingId.isBlank(), stepIndex, "bindingId");
            case BINDING_HOLD -> {
                require(bindingId != null && !bindingId.isBlank(), stepIndex, "bindingId");
                require(durationMs >= 0, stepIndex, "durationMs");
            }
            case DELAY ->
                require(durationMs >= 0, stepIndex, "durationMs");
            case SPEAK ->
                require(text != null && !text.isBlank(), stepIndex, "text");
            case RUN_COMMAND ->
                require(actionId != null && !actionId.isBlank(), stepIndex, "actionId");
        }
    }

    private static void require(boolean ok, int idx, String field) {
        if (!ok) {
            throw new IllegalArgumentException("MacroStep[" + idx + "]: " + field + " is missing or invalid");
        }
    }

    public Type getType() { return type; }
    public String getBindingId() { return bindingId; }
    public int getDurationMs() { return durationMs; }
    public String getText() { return text; }
    public String getActionId() { return actionId; }
}

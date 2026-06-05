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
        RUN_COMMAND,
        /**
         * Press an arbitrary raw key with an optional modifier and optional hold duration.
         * Requires {@code rawKey} (uppercase Elite key name, e.g. {@code "KEY_W"}).
         * {@code rawKeyModifier} is optional (e.g. {@code "KEY_LEFTCONTROL"}); {@code durationMs} is 0 for a tap.
         */
        RAW_KEY
    }

    private final Type type;
    private final String bindingId;
    private final int durationMs;
    private final String text;
    private final String actionId;
    private final String rawKey;
    private final String rawKeyModifier;

    /**
     * Creates one macro step. Only the fields required by {@code type} are used at execution time.
     */
    public MacroStep(Type type, String bindingId, int durationMs, String text, String actionId) {
        this(type, bindingId, durationMs, text, actionId, null, null);
    }

    /**
     * Creates a RAW_KEY step or any step that needs {@code rawKey}/{@code rawKeyModifier}.
     * For all other types, pass {@code null} for the last two parameters.
     */
    public MacroStep(Type type, String bindingId, int durationMs, String text, String actionId,
                     String rawKey, String rawKeyModifier) {
        this.type = type;
        this.bindingId = bindingId;
        this.durationMs = durationMs;
        this.text = text;
        this.actionId = actionId;
        this.rawKey = rawKey;
        this.rawKeyModifier = rawKeyModifier;
    }

    @SuppressWarnings("unused")
    private MacroStep() {
        type = null;
        bindingId = null;
        durationMs = 0;
        text = null;
        actionId = null;
        rawKey = null;
        rawKeyModifier = null;
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
            case RAW_KEY ->
                require(rawKey != null && !rawKey.isBlank(), stepIndex, "rawKey");
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
    public String getRawKey() { return rawKey; }
    public String getRawKeyModifier() { return rawKeyModifier; }
}

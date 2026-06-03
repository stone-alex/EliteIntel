package elite.intel.ai.hands.events;

import java.util.Objects;

/**
 * One semantic game input step inside a {@link GameInputSequenceEvent}.
 */
public final class GameInputStep {

    public enum Type {
        BINDING_TAP,
        BINDING_HOLD,
        RAW_KEY,
        TEXT,
        DELAY
    }

    private final Type type;
    private final String bindingId;
    private final int keyCode;
    private final String text;
    private final int durationMs;

    private GameInputStep(Type type, String bindingId, int keyCode, String text, int durationMs) {
        this.type = Objects.requireNonNull(type, "type");
        this.bindingId = bindingId;
        this.keyCode = keyCode;
        this.text = text;
        this.durationMs = durationMs;
    }

    /**
     * Executes a forced short tap of an Elite Dangerous binding.
     * This is the migration target for old {@code 0 = tap} inputs, especially UI/menu/tab navigation where holding
     * can overshoot.
     */
    public static GameInputStep bindingTap(String bindingId) {
        return new GameInputStep(Type.BINDING_TAP, requireBindingId(bindingId), 0, null, 0);
    }

    /**
     * Holds an Elite Dangerous binding for the requested duration in milliseconds.
     */
    public static GameInputStep bindingHold(String bindingId, int holdMs) {
        return new GameInputStep(Type.BINDING_HOLD, requireBindingId(bindingId), 0, null, requireNonNegative(holdMs, "holdMs"));
    }

    /**
     * Presses one raw physical key code through the low-level key processor.
     */
    public static GameInputStep rawKey(int keyCode) {
        return new GameInputStep(Type.RAW_KEY, null, keyCode, null, 0);
    }

    /**
     * Enters text through the low-level key processor.
     */
    public static GameInputStep text(String text) {
        return new GameInputStep(Type.TEXT, null, 0, Objects.requireNonNull(text, "text"), 0);
    }

    /**
     * Adds an explicit sequence delay. The executor's default post-input delay is not applied to this step.
     */
    public static GameInputStep delay(int delayMs) {
        return new GameInputStep(Type.DELAY, null, 0, null, requireNonNegative(delayMs, "delayMs"));
    }

    public Type getType() {
        return type;
    }

    public String getBindingId() {
        return bindingId;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public String getText() {
        return text;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public boolean isInputProducing() {
        return type != Type.DELAY;
    }

    private static String requireBindingId(String bindingId) {
        if (bindingId == null || bindingId.isBlank()) {
            throw new IllegalArgumentException("bindingId must not be blank");
        }
        return bindingId;
    }

    private static int requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }
}

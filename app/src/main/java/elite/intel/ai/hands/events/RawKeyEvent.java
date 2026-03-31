package elite.intel.ai.hands.events;

/**
 * Published when a handler needs to press a raw key by key code (not a game binding).
 * Use {@code KeyProcessor.KEY_*} constants for the keyCode.
 * Consumed by {@code HandsSubscriber} on the {@code GameControllerBus}.
 */
public class RawKeyEvent {
    private final int keyCode;

    public RawKeyEvent(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }
}

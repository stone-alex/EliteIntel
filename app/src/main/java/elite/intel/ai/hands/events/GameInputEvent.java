package elite.intel.ai.hands.events;

/**
 * Published when a handler wants to execute a game key binding with an optional hold duration.
 * Consumed by {@code HandsSubscriber} on the {@code GameControllerBus}.
 */
public class GameInputEvent {
    private final String bindingId;
    private final int holdTime;

    public GameInputEvent(String bindingId, int holdTime) {
        this.bindingId = bindingId;
        this.holdTime = holdTime;
    }

    public String getBindingId() {
        return bindingId;
    }

    public int getHoldTime() {
        return holdTime;
    }
}

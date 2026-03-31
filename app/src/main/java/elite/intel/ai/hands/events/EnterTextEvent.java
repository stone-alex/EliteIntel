package elite.intel.ai.hands.events;

/**
 * Published when a handler needs to type a string into the game (e.g. a destination name).
 * Consumed by {@code HandsSubscriber} on the {@code GameControllerBus}.
 */
public class EnterTextEvent {
    private final String text;

    public EnterTextEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

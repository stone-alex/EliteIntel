package elite.intel.ai.hands.events;

/**
 * Published when a handler wants a guaranteed single-tap of a game key binding,
 * ignoring any hold="1" attribute in the binding XML.
 * Consumed by {@code HandsSubscriber} on the {@code GameControllerBus}.
 */
public class GameTapEvent {
    private final String bindingId;

    public GameTapEvent(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getBindingId() {
        return bindingId;
    }
}

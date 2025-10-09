package elite.intel.gameapi.gamestate.status_events;

public class InGlideEvent {

    private final boolean isGliding;

    public InGlideEvent(boolean isGliding) {
        this.isGliding = isGliding;
    }

    public boolean isGliding() {
        return isGliding;
    }
}

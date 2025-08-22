package elite.companion.events;

import java.time.Duration;

public class FSDJumpEvent extends BaseEvent {
    private final String starSystem;

    public FSDJumpEvent(String timestamp, String starSystem) {
        super(timestamp, 1, Duration.ofHours(1), FSDJumpEvent.class.getName());
        this.starSystem = starSystem;
    }

    public String getStarSystem() {
        return starSystem;
    }
}
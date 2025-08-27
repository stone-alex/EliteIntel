package elite.companion.gameapi.journal.events;

import java.time.Duration;

public class NavRouteClearEvent extends BaseEvent {

    public NavRouteClearEvent(String timestamp, int priority, Duration ttl, String eventName) {
        super(timestamp, priority, ttl, eventName);
    }
}

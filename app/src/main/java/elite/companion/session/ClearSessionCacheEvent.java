package elite.companion.session;

import com.google.gson.JsonObject;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.util.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public class ClearSessionCacheEvent extends BaseEvent {

    public ClearSessionCacheEvent() {
        super(Instant.now().toString(), 1, Duration.ofDays(1), "ClearSessionCache");
    }

    @Override public String getEventType() {
        return "ClearSessionCache";
    }

    @Override public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }
}

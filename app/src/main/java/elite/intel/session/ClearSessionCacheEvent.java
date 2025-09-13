package elite.intel.session;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.BaseEvent;
import elite.intel.util.json.GsonFactory;

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

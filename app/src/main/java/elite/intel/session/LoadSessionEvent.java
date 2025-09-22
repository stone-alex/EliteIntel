package elite.intel.session;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.BaseEvent;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public class LoadSessionEvent extends BaseEvent {

    public LoadSessionEvent() {
        super(Instant.now().toString(), Duration.ofDays(1), "LoadSession");
    }

    @Override public String getEventType() {
        return "LoadSession";
    }

    @Override public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }
}

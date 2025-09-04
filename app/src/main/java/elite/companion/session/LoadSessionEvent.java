package elite.companion.session;

import com.google.gson.JsonObject;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.util.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public class LoadSessionEvent extends BaseEvent {

    public LoadSessionEvent() {
        super(Instant.now().toString(), 1, Duration.ofDays(1), "LoadSession");
    }

    @Override public String getEventType() {
        return "LoadSession";
    }

    @Override public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }
}

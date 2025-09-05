package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import elite.companion.util.GsonFactory;

import java.time.Duration;

public class NavRouteClearEvent extends BaseEvent {
    public NavRouteClearEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "NavRouteClear");
    }

    @Override
    public String getEventType() {
        return "NavRouteClear";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }
}
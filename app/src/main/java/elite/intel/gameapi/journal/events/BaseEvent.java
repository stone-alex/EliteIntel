package elite.intel.gameapi.journal.events;


import com.google.gson.JsonObject;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.time.Duration;
import java.time.Instant;

public abstract class BaseEvent implements ToJsonConvertible {

    //@SerializedName("timestamp")
    public String timestamp;
    public String eventName;
    public Instant endOfLife;

    public BaseEvent(String timestamp,  Duration ttl, String eventName) {
        this.eventName = eventName;
        this.timestamp = timestamp;
        this.endOfLife = Instant.parse(timestamp).plus(ttl);
    }

    public boolean isExpired() {
        if (endOfLife == null) {
            return false;
        }
        return Instant.now().isAfter(endOfLife);
    }


    public String getTimestamp() {
        return timestamp;
    }


    public Instant getEndOfLife() {
        return endOfLife;
    }

    public String getEventName() {
        return eventName;
    }


    public abstract String getEventType();

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    public abstract JsonObject toJsonObject();
}
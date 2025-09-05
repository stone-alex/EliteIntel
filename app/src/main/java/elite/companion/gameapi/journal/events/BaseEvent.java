package elite.companion.gameapi.journal.events;


import com.google.gson.JsonObject;
import elite.companion.util.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public abstract class BaseEvent {

    //@SerializedName("timestamp")
    public String timestamp;
    public String eventName;
    public int priority;
    public Instant endOfLife;
    public boolean isProcessed;

    public BaseEvent(String timestamp, int priority, Duration ttl, String eventName) {
        this.eventName = eventName;
        this.timestamp = timestamp;
        this.priority = priority;
        this.endOfLife = Instant.parse(timestamp).plus(ttl);
        this.isProcessed = false;
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

    public int getPriority() {
        return priority;
    }

    public Instant getEndOfLife() {
        return endOfLife;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
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
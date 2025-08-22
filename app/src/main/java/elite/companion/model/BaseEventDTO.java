package elite.companion.model;


import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.time.Instant;

public abstract class BaseEventDTO {
    @SerializedName("timestamp")
    public String timestamp;
    public int priority;
    public Instant endOfLife;
    public boolean isProcessed;

    public BaseEventDTO(String timestamp, int priority, Duration ttl) {
        this.timestamp = timestamp;
        this.priority = priority;
        this.endOfLife = Instant.parse(timestamp).plus(ttl);
        this.isProcessed = false;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(endOfLife);
    }
}
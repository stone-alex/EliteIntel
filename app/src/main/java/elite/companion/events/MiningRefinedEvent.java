package elite.companion.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.time.Instant;

public class MiningRefinedEvent extends BaseEventEvent {

    @SerializedName("mineralType")
    private String mineralType;

    public MiningRefinedEvent() {
        super(String.valueOf(Instant.now()), 2, Duration.ofMinutes(10), MiningRefinedEvent.class.getName()); // Medium priority, short TTL
    }

    public String getMineralType() {
        return mineralType;
    }

    public void setMineralType(String mineralType) {
        this.mineralType = mineralType;
    }
}

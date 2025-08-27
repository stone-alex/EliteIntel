package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;

public class FSDTargetEvent extends BaseEvent {
    @SerializedName("Name")
    private String name;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("StarClass")
    private String starClass;

    @SerializedName("RemainingJumpsInRoute")
    private int remainingJumpsInRoute;

    public FSDTargetEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), FSDTargetEvent.class.getName());
    }

    // Getters
    public String getName() {
        return name;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public String getStarClass() {
        return starClass;
    }

    public int getRemainingJumpsInRoute() {
        return remainingJumpsInRoute;
    }
}
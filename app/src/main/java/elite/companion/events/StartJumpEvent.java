package elite.companion.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.util.Set;
import java.util.HashSet;

public class StartJumpEvent extends BaseEvent {

    @SerializedName("JumpType")
    private String jumpType;

    @SerializedName("Taxi")
    private boolean taxi;

    @SerializedName("StarSystem")
    private String starSystem;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("StarClass")
    private String starClass;

    private final boolean isScoopable;

    private static final Set<String> SCOOPABLE_STARS = new HashSet<>(Set.of("K", "G", "B", "F", "O", "A", "M"));

    public StartJumpEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), StartJumpEvent.class.getName());
        this.isScoopable = starClass != null && SCOOPABLE_STARS.contains(starClass.toUpperCase());
    }

    public String getJumpType() {
        return jumpType;
    }

    public boolean isTaxi() {
        return taxi;
    }

    public String getStarSystem() {
        return starSystem;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public String getStarClass() {
        return starClass;
    }

    public boolean isScoopable() {
        return isScoopable;
    }
}
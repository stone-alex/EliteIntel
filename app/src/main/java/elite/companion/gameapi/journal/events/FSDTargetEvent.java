package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
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

    public FSDTargetEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "FSDTarget");
        FSDTargetEvent event = GsonFactory.getGson().fromJson(json, FSDTargetEvent.class);
        this.name = event.name;
        this.systemAddress = event.systemAddress;
        this.starClass = event.starClass;
        this.remainingJumpsInRoute = event.remainingJumpsInRoute;
    }

    @Override
    public String getEventType() {
        return "FSDTarget";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

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
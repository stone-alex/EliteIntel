package elite.intel.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.intel.util.TimestampFormatter;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;

public class ApproachBodyEvent extends BaseEvent {
    @SerializedName("StarSystem")
    private String starSystem;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("Body")
    private String body;

    @SerializedName("BodyID")
    private int bodyID;

    public ApproachBodyEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "ApproachBody");
        ApproachBodyEvent event = GsonFactory.getGson().fromJson(json, ApproachBodyEvent.class);
        this.starSystem = event.starSystem;
        this.systemAddress = event.systemAddress;
        this.body = event.body;
        this.bodyID = event.bodyID;
    }

    @Override
    public String getEventType() {
        return "ApproachBody";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public String getStarSystem() {
        return starSystem;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public String getBody() {
        return body;
    }

    public int getBodyID() {
        return bodyID;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}
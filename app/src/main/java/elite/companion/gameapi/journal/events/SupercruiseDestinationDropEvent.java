package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;
import java.util.StringJoiner;

public class SupercruiseDestinationDropEvent extends BaseEvent {
    @SerializedName("Type")
    private String type;

    @SerializedName("Threat")
    private int threat;

    @SerializedName("MarketID")
    private long marketID;

    public SupercruiseDestinationDropEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "SupercruiseDestinationDrop");
        SupercruiseDestinationDropEvent event = GsonFactory.getGson().fromJson(json, SupercruiseDestinationDropEvent.class);
        this.type = event.type;
        this.threat = event.threat;
        this.marketID = event.marketID;
    }

    @Override
    public String getEventType() {
        return "SupercruiseDestinationDrop";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public String getType() {
        return type;
    }

    public int getThreat() {
        return threat;
    }

    public long getMarketID() {
        return marketID;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    @Override
    public String toString() {
        return new StringJoiner("Supercruise destination drop: ")
                .add("type='" + type + "'")
                .add("threat=" + threat)
                .add("marketID=" + marketID)
                .toString();
    }
}
package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
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

    public SupercruiseDestinationDropEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), SupercruiseDestinationDropEvent.class.getName());
    }

    // Getters
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
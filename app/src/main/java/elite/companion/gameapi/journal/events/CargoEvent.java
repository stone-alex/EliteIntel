package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;
import java.util.StringJoiner;

public class CargoEvent extends BaseEvent {
    @SerializedName("Vessel")
    private String vessel;

    @SerializedName("Count")
    private int count;

    public CargoEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), CargoEvent.class.getName());
    }

    // Getters
    public String getVessel() {
        return vessel;
    }

    public int getCount() {
        return count;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    @Override
    public String toString() {
        return new StringJoiner("Cargo detected: ")
                .add("vessel='" + vessel + "'")
                .add("count=" + count)
                .toString();
    }
}
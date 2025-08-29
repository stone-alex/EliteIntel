package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;

public class MissionAbandonedEvent extends BaseEvent {
    @SerializedName("Name")
    private String name;

    @SerializedName("LocalisedName")
    private String localisedName;

    @SerializedName("MissionID")
    private long missionID;

    public MissionAbandonedEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), MissionAbandonedEvent.class.getName());
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getLocalisedName() {
        return localisedName;
    }

    public long getMissionID() {
        return missionID;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}
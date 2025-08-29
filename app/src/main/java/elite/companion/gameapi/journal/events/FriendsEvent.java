package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;

public class FriendsEvent extends BaseEvent {
    @SerializedName("Status")
    private String status;

    @SerializedName("Name")
    private String name;

    public FriendsEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), FriendsEvent.class.getName());
    }

    // Getters
    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}
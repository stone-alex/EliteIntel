package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;

public class FriendsEvent extends BaseEvent {
    @SerializedName("Status")
    private String status;

    @SerializedName("Name")
    private String name;

    public FriendsEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "Friends");
        FriendsEvent event = GsonFactory.getGson().fromJson(json, FriendsEvent.class);
        this.status = event.status;
        this.name = event.name;
    }

    @Override
    public String getEventType() {
        return "Friends";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

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
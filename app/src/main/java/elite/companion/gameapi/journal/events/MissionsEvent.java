package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;
import java.util.List;

public class MissionsEvent extends BaseEvent {
    public static class Mission {
        @SerializedName("MissionID")
        private long missionID;

        @SerializedName("Name")
        private String name;

        @SerializedName("PassengerMission")
        private boolean passengerMission;

        @SerializedName("Expires")
        private long expires;

        public long getMissionID() {
            return missionID;
        }

        public String getName() {
            return name;
        }

        public boolean isPassengerMission() {
            return passengerMission;
        }

        public long getExpires() {
            return expires;
        }
    }

    @SerializedName("Active")
    private List<Mission> active;

    @SerializedName("Failed")
    private List<Mission> failed;

    @SerializedName("Complete")
    private List<Mission> complete;

    public MissionsEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "Missions");
        MissionsEvent event = GsonFactory.getGson().fromJson(json, MissionsEvent.class);
        this.active = event.active;
        this.failed = event.failed;
        this.complete = event.complete;
    }

    @Override
    public String getEventType() {
        return "Missions";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public List<Mission> getActive() {
        return active;
    }

    public List<Mission> getFailed() {
        return failed;
    }

    public List<Mission> getComplete() {
        return complete;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}
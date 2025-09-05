package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;
import java.util.List;

public class EngineerProgressEvent extends BaseEvent {
    @SerializedName("Engineers")
    private List<Engineer> engineers;

    public EngineerProgressEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofDays(30), "EngineerProgress");
        EngineerProgressEvent event = GsonFactory.getGson().fromJson(json, EngineerProgressEvent.class);
        this.engineers = event.engineers;
    }

    @Override
    public String getEventType() {
        return "EngineerProgress";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public List<Engineer> getEngineers() {
        return engineers;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    public static class Engineer {
        @SerializedName("Engineer")
        private String name;

        @SerializedName("EngineerID")
        private long engineerID;

        @SerializedName("Progress")
        private String progress;

        @SerializedName("RankProgress")
        private int rankProgress;

        @SerializedName("Rank")
        private int rank;

        public String getName() {
            return name;
        }

        public long getEngineerID() {
            return engineerID;
        }

        public String getProgress() {
            return progress;
        }

        public int getRankProgress() {
            return rankProgress;
        }

        public int getRank() {
            return rank;
        }

        public boolean isFullyUnlocked() {
            return "Unlocked".equals(progress) && rank == 5 && rankProgress == 0;
        }
    }
}
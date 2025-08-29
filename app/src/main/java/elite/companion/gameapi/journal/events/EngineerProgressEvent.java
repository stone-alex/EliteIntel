package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;
import java.util.List;

public class EngineerProgressEvent extends BaseEvent {
    @SerializedName("Engineers")
    private List<Engineer> engineers;

    public EngineerProgressEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), EngineerProgressEvent.class.getName());
    }

    // Getter
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

        // Getters
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

        // Helper to check if engineer is fully unlocked (Rank 5, no progress needed)
        public boolean isFullyUnlocked() {
            return "Unlocked".equals(progress) && rank == 5 && rankProgress == 0;
        }
    }
}
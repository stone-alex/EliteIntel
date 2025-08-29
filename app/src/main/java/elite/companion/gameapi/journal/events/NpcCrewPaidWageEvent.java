package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;
import java.util.StringJoiner;

public class NpcCrewPaidWageEvent extends BaseEvent {
    @SerializedName("NpcCrewName")
    private String npcCrewName;

    @SerializedName("NpcCrewId")
    private long npcCrewId;

    @SerializedName("Amount")
    private long amount;

    public NpcCrewPaidWageEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), NpcCrewPaidWageEvent.class.getName());
    }

    // Getters
    public String getNpcCrewName() {
        return npcCrewName;
    }

    public long getNpcCrewId() {
        return npcCrewId;
    }

    public long getAmount() {
        return amount;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    @Override
    public String toString() {
        return new StringJoiner("NPC crew wage paid: ")
                .add("npcCrewName='" + npcCrewName + "'")
                .add("npcCrewId=" + npcCrewId)
                .add("amount=" + amount)
                .toString();
    }
}
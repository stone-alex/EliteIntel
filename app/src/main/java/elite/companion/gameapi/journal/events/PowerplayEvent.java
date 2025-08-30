package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

public class PowerplayEvent extends BaseEvent {
    @SerializedName("Power")
    private String power;

    @SerializedName("Rank")
    private int rank;

    @SerializedName("Merits")
    private int merits;

    @SerializedName("TimePledged")
    private long timePledged;

    public PowerplayEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "PlayerPower");
        PowerplayEvent event = GsonFactory.getGson().fromJson(json, PowerplayEvent.class);
        this.power = event.power;
        this.rank = event.rank;
        this.merits = event.merits;
        this.timePledged = event.timePledged;
    }

    @Override
    public String getEventType() {
        return "PlayerPower";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getMerits() {
        return merits;
    }

    public void setMerits(int merits) {
        this.merits = merits;
    }

    public long getTimePledged() {
        return timePledged;
    }

    public void setTimePledged(long timePledged) {
        this.timePledged = timePledged;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PowerplayEvent that = (PowerplayEvent) o;
        return getRank() == that.getRank() && getMerits() == that.getMerits() && getTimePledged() == that.getTimePledged() && Objects.equals(getPower(), that.getPower());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getPower());
        result = 31 * result + getRank();
        result = 31 * result + getMerits();
        result = 31 * result + Long.hashCode(getTimePledged());
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PowerplayEvent.class.getSimpleName() + "[", "]")
                .add("power='" + power + "'")
                .add("rank=" + rank)
                .add("merits=" + merits)
                .add("timePledged=" + timePledged)
                .add("timestamp='" + timestamp + "'")
                .add("eventName='" + eventName + "'")
                .add("priority=" + priority)
                .add("endOfLife=" + endOfLife)
                .add("isProcessed=" + isProcessed)
                .toString();
    }
}
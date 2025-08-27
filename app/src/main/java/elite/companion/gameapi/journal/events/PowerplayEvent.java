package elite.companion.gameapi.journal.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;

public class PowerplayEvent extends BaseEvent {
    private String power;
    private int rank;
    private int merits;
    private long timePledged;

    public PowerplayEvent() {
        super(String.valueOf(Instant.now()), 1, Duration.ofSeconds(30), PowerplayEvent.class.getName());
    }

    public String getPower() {
        return power;
    }

    public int getRank() {
        return rank;
    }

    public int getMerits() {
        return merits;
    }

    public long getTimePledged() {
        return timePledged;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setMerits(int merits) {
        this.merits = merits;
    }

    public void setTimePledged(long timePledged) {
        this.timePledged = timePledged;
    }

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        PowerplayEvent that = (PowerplayEvent) o;
        return getRank() == that.getRank() && getMerits() == that.getMerits() && getTimePledged() == that.getTimePledged() && Objects.equals(getPower(), that.getPower());
    }

    @Override public int hashCode() {
        int result = Objects.hashCode(getPower());
        result = 31 * result + getRank();
        result = 31 * result + getMerits();
        result = 31 * result + Long.hashCode(getTimePledged());
        return result;
    }

    @Override public String toString() {
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

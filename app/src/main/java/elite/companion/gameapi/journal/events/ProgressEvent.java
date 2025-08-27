package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;

public class ProgressEvent extends BaseEvent {
    @SerializedName("Combat")
    private int combat;

    @SerializedName("Trade")
    private int trade;

    @SerializedName("Explore")
    private int explore;

    @SerializedName("Soldier")
    private int soldier;

    @SerializedName("Exobiologist")
    private int exobiologist;

    @SerializedName("Empire")
    private int empire;

    @SerializedName("Federation")
    private int federation;

    @SerializedName("CQC")
    private int cqc;

    public ProgressEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), ProgressEvent.class.getName());
    }

    public int getCombat() {
        return combat;
    }

    public int getTrade() {
        return trade;
    }

    public int getExplore() {
        return explore;
    }

    public int getSoldier() {
        return soldier;
    }

    public int getExobiologist() {
        return exobiologist;
    }

    public int getEmpire() {
        return empire;
    }

    public int getFederation() {
        return federation;
    }

    public int getCQC() {
        return cqc;
    }
}
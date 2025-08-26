package elite.companion.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;

public class ReputationEvent extends BaseEvent {
    @SerializedName("Empire")
    private double empire;

    @SerializedName("Federation")
    private double federation;

    @SerializedName("Independent")
    private double independent;

    @SerializedName("Alliance")
    private double alliance;

    public ReputationEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), ReputationEvent.class.getName());
    }

    public double getEmpire() {
        return empire;
    }

    public double getFederation() {
        return federation;
    }

    public double getIndependent() {
        return independent;
    }

    public double getAlliance() {
        return alliance;
    }
}
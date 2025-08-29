package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;
import java.text.DecimalFormat;

public class ReputationEvent extends BaseEvent {
    @SerializedName("Empire")
    private double empire;

    @SerializedName("Federation")
    private double federation;

    @SerializedName("Independent")
    private double independent;

    @SerializedName("Alliance")
    private double alliance;

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00%");

    public ReputationEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), ReputationEvent.class.getName());
    }

    // Getters (raw doubles)
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

    // Helper methods to format as percentages
    public String getEmpirePercent() {
        return PERCENT_FORMAT.format(empire / 100.0);
    }

    public String getFederationPercent() {
        return PERCENT_FORMAT.format(federation / 100.0);
    }

    public String getIndependentPercent() {
        return PERCENT_FORMAT.format(independent / 100.0);
    }

    public String getAlliancePercent() {
        return PERCENT_FORMAT.format(alliance / 100.0);
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}
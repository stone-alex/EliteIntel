package elite.companion.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class FSSSignalDiscoveredEvent extends BaseEvent {
    private static final Logger logger = LoggerFactory.getLogger(FSSSignalDiscoveredEvent.class);

    @SerializedName("SystemAddress")
    private final long systemAddress;

    @SerializedName("SignalName")
    private final String signalName;

    @SerializedName("SignalType")
    private final String signalType;

    @SerializedName("SignalName_Localised")
    private final String signalNameLocalised; // Nullable

    @SerializedName("IsStation")
    private final Boolean isStation; // Nullable, defaults to null if absent

    public FSSSignalDiscoveredEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 3, Duration.ofMinutes(30), FSSSignalDiscoveredEvent.class.getName());

        // Debug log to inspect JSON input
        logger.debug("Parsing FSSSignalDiscoveredEvent: {}", json.toString());

        this.systemAddress = json.get("SystemAddress").getAsLong();
        this.signalName = json.get("SignalName").getAsString();
        this.signalType = json.get("SignalType").getAsString();
        this.signalNameLocalised = json.has("SignalName_Localised") ? json.get("SignalName_Localised").getAsString() : null;
        this.isStation = json.has("IsStation") ? json.get("IsStation").getAsBoolean() : null;

        // Debug log to verify field values
        logger.debug("Parsed values: systemAddress={}, signalName={}, signalType={}, signalNameLocalised={}, isStation={}",
                systemAddress, signalName, signalType, signalNameLocalised, isStation);
    }

    // Getters
    public long getSystemAddress() {
        return systemAddress;
    }

    public String getSignalName() {
        return signalName;
    }

    public String getSignalType() {
        return signalType;
    }

    public String getSignalNameLocalised() {
        return signalNameLocalised;
    }

    public Boolean getIsStation() {
        return isStation;
    }

    // Helper methods
    public boolean isFleetCarrier() {
        return "FleetCarrier".equals(signalType);
    }

    public boolean isResourceExtractionSite() {
        return "ResourceExtraction".equals(signalType);
    }

    public boolean isInstallation() {
        return "Installation".equals(signalType);
    }
}
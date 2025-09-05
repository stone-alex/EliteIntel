package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
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
    private final String signalNameLocalised;

    @SerializedName("IsStation")
    private final Boolean isStation;

    public FSSSignalDiscoveredEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 3, Duration.ofSeconds(30), "FSSSignalDiscovered");
        logger.debug("Parsing FSSSignalDiscoveredEvent: {}", json.toString());
        FSSSignalDiscoveredEvent event = GsonFactory.getGson().fromJson(json, FSSSignalDiscoveredEvent.class);
        this.systemAddress = event.systemAddress;
        this.signalName = event.signalName;
        this.signalType = event.signalType;
        this.signalNameLocalised = event.signalNameLocalised;
        this.isStation = event.isStation;
        logger.debug("Parsed values: systemAddress={}, signalName={}, signalType={}, signalNameLocalised={}, isStation={}",
                systemAddress, signalName, signalType, signalNameLocalised, isStation);
    }

    @Override
    public String getEventType() {
        return "FSSSignalDiscovered";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

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
package elite.companion.events;

import com.google.gson.annotations.SerializedName;
import java.time.Duration;

public class FSSSignalDiscoveredEvent extends BaseEvent {

    @SerializedName("SystemAddress")
    public long systemAddress;

    @SerializedName("SignalName")
    public String signalName;

    @SerializedName("SignalName_Localised")
    public String signalNameLocalised;

    @SerializedName("SignalType")
    public String signalType;

    @SerializedName("IsStation")
    public Boolean isStation; // Boolean to handle true/false or null

    public FSSSignalDiscoveredEvent(String timestamp, long systemAddress, String signalName, String signalNameLocalised, String signalType, Boolean isStation) {
        super(timestamp, 3, Duration.ofMinutes(30), FSSSignalDiscoveredEvent.class.getName());
        this.systemAddress = systemAddress;
        this.signalName = signalName;
        this.signalNameLocalised = signalNameLocalised;
        this.signalType = signalType;
        this.isStation = isStation;
    }

    // Helper method to get localized name (fallback to signalName if null)
    public String getDisplayName() {
        return signalNameLocalised != null ? signalNameLocalised : signalName;
    }

    // Helper method to get full description
    public String getDescription() {
        String type = signalType != null ? " " + signalType : "";
        String station = isStation != null && isStation ? " (Station)" : "";
        return getDisplayName() + type + station;
    }

    @Override
    public String toString() {
        return String.format("%s: FSS Signal Discovered - %s (%s) in system %d", timestamp, getDisplayName(), signalType, systemAddress);
    }
}
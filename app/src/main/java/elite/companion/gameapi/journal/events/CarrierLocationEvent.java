package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;
import java.time.Duration;

public class CarrierLocationEvent extends BaseEvent {
    @SerializedName("CarrierType")
    private String carrierType;

    @SerializedName("CarrierID")
    private long carrierID;

    @SerializedName("StarSystem")
    private String starSystem;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("BodyID")
    private int bodyID;

    public CarrierLocationEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), CarrierLocationEvent.class.getName());
    }

    // Getters
    public String getCarrierType() {
        return carrierType;
    }

    public long getCarrierID() {
        return carrierID;
    }

    public String getStarSystem() {
        return starSystem;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public int getBodyID() {
        return bodyID;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}
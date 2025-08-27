package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;

public class CarrierJumpRequestEvent extends BaseEvent {
    @SerializedName("CarrierType")
    private String carrierType;

    @SerializedName("CarrierID")
    private long carrierId;

    @SerializedName("SystemName")
    private String systemName;

    @SerializedName("Body")
    private String body;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("BodyID")
    private int bodyId;

    @SerializedName("DepartureTime")
    private String departureTime;

    public CarrierJumpRequestEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), CarrierJumpRequestEvent.class.getName());
    }

    // Getters
    public String getCarrierType() {
        return carrierType;
    }

    public long getCarrierId() {
        return carrierId;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getBody() {
        return body;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public int getBodyId() {
        return bodyId;
    }

    public String getDepartureTime() {
        return departureTime;
    }
}
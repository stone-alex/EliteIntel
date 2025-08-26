package elite.companion.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;

public class ShipTargetedEvent extends BaseEvent {
    @SerializedName("TargetLocked")
    private boolean targetLocked;

    @SerializedName("Ship")
    private String ship;

    @SerializedName("ScanStage")
    private int scanStage;

    @SerializedName("PilotName")
    private String pilotName;

    @SerializedName("PilotName_Localised")
    private String pilotNameLocalised;

    @SerializedName("PilotRank")
    private String pilotRank;

    @SerializedName("ShieldHealth")
    private float shieldHealth;

    @SerializedName("HullHealth")
    private float hullHealth;

    @SerializedName("LegalStatus")
    private String legalStatus;

    @SerializedName("Power")
    private String pledgePower;

    @SerializedName("Faction")
    private String faction;

    @SerializedName("Ship_Localised")
    private String shipLocalised;

    public ShipTargetedEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), ShipTargetedEvent.class.getName());
    }

    // Getters
    public boolean isTargetLocked() {
        return targetLocked;
    }

    public String getShip() {
        return ship;
    }

    public int getScanStage() {
        return scanStage;
    }

    public String getPilotName() {
        return pilotName;
    }

    public String getPilotNameLocalised() {
        return pilotNameLocalised;
    }

    public String getPilotRank() {
        return pilotRank;
    }

    public float getShieldHealth() {
        return shieldHealth;
    }

    public float getHullHealth() {
        return hullHealth;
    }

    public String getLegalStatus() {
        return legalStatus;
    }


    public String getPledgePower() {
        return pledgePower;
    }

    public String getFaction() {
        return faction;
    }


    public String getShipLocalised() {
        return shipLocalised;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}
package elite.companion.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;

public class TouchdownEvent extends BaseEvent {
    @SerializedName("PlayerControlled")
    private boolean playerControlled;

    @SerializedName("Taxi")
    private boolean taxi;

    @SerializedName("Multicrew")
    private boolean multicrew;

    @SerializedName("StarSystem")
    private String starSystem;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("Body")
    private String body;

    @SerializedName("BodyID")
    private int bodyId;

    @SerializedName("OnStation")
    private boolean onStation;

    @SerializedName("OnPlanet")
    private boolean onPlanet;

    @SerializedName("Latitude")
    private float latitude;

    @SerializedName("Longitude")
    private float longitude;

    @SerializedName("NearestDestination")
    private String nearestDestination;

    @SerializedName("NearestDestination_Localised")
    private String nearestDestinationLocalised;

    public TouchdownEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), TouchdownEvent.class.getName());
    }

    // Getters
    public boolean isPlayerControlled() {
        return playerControlled;
    }

    public boolean isTaxi() {
        return taxi;
    }

    public boolean isMulticrew() {
        return multicrew;
    }

    public String getStarSystem() {
        return starSystem;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public String getBody() {
        return body;
    }

    public int getBodyId() {
        return bodyId;
    }

    public boolean isOnStation() {
        return onStation;
    }

    public boolean isOnPlanet() {
        return onPlanet;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getNearestDestination() {
        return nearestDestination;
    }

    public String getNearestDestinationLocalised() {
        return nearestDestinationLocalised;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }
}
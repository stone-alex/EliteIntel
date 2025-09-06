package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;
import elite.companion.util.json.GsonFactory;

import java.time.Duration;

public class LiftoffEvent extends BaseEvent {
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

    public LiftoffEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "Liftoff");
        LiftoffEvent event = GsonFactory.getGson().fromJson(json, LiftoffEvent.class);
        this.playerControlled = event.playerControlled;
        this.taxi = event.taxi;
        this.multicrew = event.multicrew;
        this.starSystem = event.starSystem;
        this.systemAddress = event.systemAddress;
        this.body = event.body;
        this.bodyId = event.bodyId;
        this.onStation = event.onStation;
        this.onPlanet = event.onPlanet;
        this.latitude = event.latitude;
        this.longitude = event.longitude;
        this.nearestDestination = event.nearestDestination;
        this.nearestDestinationLocalised = event.nearestDestinationLocalised;
    }

    @Override
    public String getEventType() {
        return "Liftoff";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

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
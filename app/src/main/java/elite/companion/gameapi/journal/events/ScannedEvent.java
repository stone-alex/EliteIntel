package elite.companion.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.companion.util.GsonFactory;
import java.time.Duration;

public class ScannedEvent extends BaseEvent {
    @SerializedName("ScanType")
    private String scanType;

    public ScannedEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "Scanned");
        ScannedEvent event = GsonFactory.getGson().fromJson(json, ScannedEvent.class);
        this.scanType = event.scanType;
    }

    @Override
    public String getEventType() {
        return "Scanned";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public String getScanType() {
        return scanType;
    }
}
package elite.companion.gameapi;

import com.google.gson.JsonObject;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.util.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public class SensorDataEvent extends BaseEvent {


    public SensorDataEvent(String sensorData) {
        super(Instant.now().toString(), 1, Duration.ofSeconds(10), "SensorData");
        this.sensorData = "Notify user: "+sensorData;
        this.confidence = 100;
    }


    private String sensorData;
    private float confidence;


    public String getSensorData() {
        return sensorData;
    }

    public float getConfidence() {
        return confidence;
    }

    @Override public String getEventType() {
        return "UserInput";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }


}

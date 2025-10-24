package elite.intel.gameapi;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.BaseEvent;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;
import java.time.Instant;

public class SensorDataEvent extends BaseEvent {

    public SensorDataEvent(String sensorData) {
        super(Instant.now().toString(), Duration.ofSeconds(10), "SensorData");
        this.sensorData = sensorData;
    }

    private String sensorData;

    public String getSensorData() {
        return sensorData;
    }

    @Override public String getEventType() {
        return "UserInput";
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }
}

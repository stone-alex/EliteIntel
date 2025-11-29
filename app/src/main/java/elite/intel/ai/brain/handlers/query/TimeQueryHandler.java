package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.time.Clock;
import java.time.Instant;

import static elite.intel.ai.brain.handlers.query.Queries.TIME_IN_ZONE;

public class TimeQueryHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing temporal data... Stand by..."));

        Instant instant = Clock.systemUTC().instant();
        String utcTime = instant.toString();

        return process(new AiDataStruct(TIME_IN_ZONE.getInstructions(), new DataDto(utcTime)), originalUserInput);
    }


    record DataDto(String utcTime) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

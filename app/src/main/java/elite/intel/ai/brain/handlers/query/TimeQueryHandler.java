package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeQueryHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        if (SystemSession.getInstance().isRunningLocalLLM()) {
            ZonedDateTime localNow = ZonedDateTime.now(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
            String formattedTime = localNow.format(formatter);
            return process("Only local time available: " + formattedTime+" time in other timezone is not available");
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Analyzing temporal data... Stand by..."));

            Instant instant = Clock.systemUTC().instant();
            String utcTime = instant.toString();
            String instructions = """
                    Calculate current time at the location requested by user. The data provides current UTC time
                    """;
            return process(new AiDataStruct(instructions, new DataDto(utcTime)), originalUserInput);
        }
    }


    record DataDto(String utcTime) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

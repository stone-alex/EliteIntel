package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.TimestampFormatter;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CarrierETAHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing fleet carrier telemetry. Stand by."));
        PlayerSession playerSession = PlayerSession.getInstance();
        String carrierDepartureTime = playerSession.getCarrierDepartureTime();
        String now = TimestampFormatter.formatTimestamp(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), true);

        String instructions = """
                Calculate fleet carrier Estimated Time of Arrival using arrival and current time.
                Return ETA in minutes. (do not return decimals, whole numbers only)
                Example: "Estimated Time of Arrival is Y minutes"
                """;

        return process(new AiDataStruct(instructions, new DataDto(carrierDepartureTime, now)), originalUserInput);
    }


    record DataDto(String arrivalTime, String now) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

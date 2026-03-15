package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class CarrierETAHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing fleet carrier telemetry. Stand by."));
        PlayerSession playerSession = PlayerSession.getInstance();
        String carrierDepartureTime = playerSession.getCarrierDepartureTime();
        if (carrierDepartureTime == null) {
            return process("No carrier departure time available.");
        }

        long minutesUntilArrival;
        try {
            ZonedDateTime arrival = ZonedDateTime.parse(carrierDepartureTime, DateTimeFormatter.ISO_DATE_TIME);
            minutesUntilArrival = ChronoUnit.MINUTES.between(ZonedDateTime.now(), arrival);
        } catch (Exception e) {
            return process("Unable to parse carrier arrival time.");
        }

        String instructions = """
                Report the fleet carrier estimated time of arrival.
                
                Data fields:
                - minutesUntilArrival: pre-computed minutes until the carrier arrives (negative means already arrived)
                
                State the arrival time in minutes. If negative, say the carrier has already arrived.
                """;

        return process(new AiDataStruct(instructions, new DataDto(minutesUntilArrival)), originalUserInput);
    }


    record DataDto(long minutesUntilArrival) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

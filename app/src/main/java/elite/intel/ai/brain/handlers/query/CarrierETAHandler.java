package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.session.PlayerSession;
import elite.intel.util.TimestampFormatter;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static elite.intel.ai.brain.handlers.query.Queries.CARRIER_ETA;

public class CarrierETAHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        String carrierDepartureTime = playerSession.getCarrierDepartureTime();
        String now = TimestampFormatter.formatTimestamp(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), true);
        return process(new AiDataStruct(CARRIER_ETA.getInstructions(), new DataDto(carrierDepartureTime, now)), originalUserInput);
    }


    record DataDto(String arrivalTime, String now) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

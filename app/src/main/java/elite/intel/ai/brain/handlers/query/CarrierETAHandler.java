package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.TimestampFormatter;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CarrierETAHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object obj = playerSession.get(PlayerSession.CARRIER_DEPARTURE_TIME);
        if(obj == null) {
            return analyzeData(toJson("No ETA available. Fleet carrier is located in "+playerSession.getCarrierData().getLocation()), originalUserInput);
        }

        String departureTime = String.valueOf(obj);
        String now = TimestampFormatter.formatTimestamp(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), true);
        return analyzeData(new DataDto(departureTime, now).toJson(), originalUserInput);
    }


    record DataDto(String arrivalTime, String now) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

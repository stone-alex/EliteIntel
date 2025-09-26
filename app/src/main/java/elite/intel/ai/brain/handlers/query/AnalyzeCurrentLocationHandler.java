package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCurrentLocationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (currentLocation == null) {
            return analyzeData(GsonFactory.getGson().toJson("Current location data is not available"), originalUserInput);
        } else {
            return analyzeData(new DataDto(playerSession.getCurrentLocation()).toJson(), originalUserInput);
        }
    }

    record DataDto(LocationDto location) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

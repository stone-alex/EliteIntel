package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeLocalSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (currentLocation.getBodyId() < 0) return analyzeData(toJson("No data available"), originalUserInput);

        StarSystemDto edsmData = EdsmApiClient.searchStarSystem(currentLocation.getStarName(), 1);

        if (edsmData.getData() == null) {
            return analyzeData(currentLocation.toJson(), originalUserInput);
        } else {
            return analyzeData(new DataDto(currentLocation, edsmData).toJson(), originalUserInput);
        }
    }

    record DataDto(LocationDto currentLocation, StarSystemDto edsmData) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

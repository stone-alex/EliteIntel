package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Map;

public class AnalyzeMaterialsOnPlanetHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if(currentLocation == null) return analyzeData(toJson("No data available"), originalUserInput);

        if (currentLocation.getMaterials().isEmpty()) {
            return analyzeData(toJson(" no data available..."), originalUserInput);
        } else {
            return analyzeData(new DataDto(currentLocation.getMaterials()).toJson(), originalUserInput);
        }
    }

    record DataDto(Map<String, Double> materials) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

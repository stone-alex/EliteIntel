package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;
import java.util.Map;

public class AnalyzeMaterialsOnPlanetHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if(currentLocation == null) return analyzeData(toJson("No data available"), originalUserInput);
        Map<String, Double> materials = currentLocation.getMaterials();
        List<SAASignalsFoundEvent.Genus> species = currentLocation.getGenus();

        if (materials.isEmpty() && species.isEmpty()) {
            return analyzeData(toJson(" no data available..."), originalUserInput);
        } else {
            return analyzeData(new DataDto(materials, species).toJson(), originalUserInput);
        }
    }

    record DataDto(Map<String, Double> materials, List<SAASignalsFoundEvent.Genus> species) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

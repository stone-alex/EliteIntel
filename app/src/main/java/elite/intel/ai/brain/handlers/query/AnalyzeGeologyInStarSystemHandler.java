package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.HashMap;
import java.util.Map;

public class AnalyzeGeologyInStarSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        Map<String, Integer> planetsWithGeoSignals = planetsWithGeoSignals();
        String instructions = """
                    Data contains a map of planet name to amount of geo signals
                """;
        return process(new AiDataStruct(instructions, new DataDto(planetsWithGeoSignals)), originalUserInput);
    }

    private Map<String, Integer> planetsWithGeoSignals() {
        Map<String, Integer> result = new HashMap<>();
        Map<Long, LocationDto> locations = playerSession.getLocations();
        for (LocationDto location : locations.values()) {
            if (location.getGeoSignals() > 0) {
                result.put(location.getPlanetName(), location.getGeoSignals());
            }
        }
        return result;
    }

    record DataDto(Map<String, Integer> planetsWithGeoSignals) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

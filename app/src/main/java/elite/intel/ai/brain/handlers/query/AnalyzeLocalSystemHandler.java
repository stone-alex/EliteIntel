package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeLocalSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (currentLocation.getBodyId() < 0) return process("No data available");

        StarSystemDto edsmData = EdsmApiClient.searchStarSystem(currentLocation.getStarName(), 1);

        if (edsmData.getData() == null) {
            return process(new DataDto("Use currentLocation data to provide answers.", currentLocation, null), originalUserInput);
        } else {
            return process(new DataDto("Use currentLocation in combination with EDSM data to provide answers.", currentLocation, edsmData), originalUserInput);
        }
    }

    record DataDto(String instructions, ToJsonConvertible currentLocation, ToJsonConvertible edsmData) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

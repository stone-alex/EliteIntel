package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeLocalSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto location = playerSession.getPrimarySystem();

        location = playerSession.getPrimarySystem();
        if (location.getBodyId() < 0) return process("No data available");

        StarSystemDto edsmData = EdsmApiClient.searchStarSystem(location.getStarName(), 1);
        TrafficDto trafficDto = EdsmApiClient.searchTraffic(location.getStarName());
        DeathsDto deathsDto = EdsmApiClient.searchDeaths(location.getStarName());

        if (edsmData.getData() == null) {
            return process(new DataDto("Use currentStarSystem data to provide answers.", location, null, null, null), originalUserInput);
        } else {
            return process(new DataDto("Use currentStarSystem in combination with EDSM data to provide answers.", location, edsmData, trafficDto, deathsDto), originalUserInput);
        }
    }

    record DataDto(String instructions, ToJsonConvertible currentStarSystem, ToJsonConvertible edsmData, TrafficDto trafficData, DeathsDto deathsData) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

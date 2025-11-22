package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.DeathsDto;
import elite.intel.search.edsm.dto.StarSystemDto;
import elite.intel.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeLocalSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto location = playerSession.getPrimaryStarLocation();

        location = playerSession.getPrimaryStarLocation();
        if (location.getBodyId() < 0) return process("No data available");

        StarSystemDto edsmData = EdsmApiClient.searchStarSystem(location.getStarName(), 1);
        TrafficDto trafficDto = EdsmApiClient.searchTraffic(location.getStarName());
        DeathsDto deathsDto = EdsmApiClient.searchDeaths(location.getStarName());

        if (edsmData.getData() == null) {
            return process(new AiDataStruct("Use currentStarSystem data to provide answers.", new DataDto(location, null, null, null)), originalUserInput);
        } else {
            return process(new AiDataStruct("Use currentStarSystem in combination with EDSM data to provide answers.", new DataDto(location, edsmData, trafficDto, deathsDto)), originalUserInput);
        }
    }

    record DataDto(ToJsonConvertible currentStarSystem, ToJsonConvertible edsmData, TrafficDto trafficData, DeathsDto deathsData) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

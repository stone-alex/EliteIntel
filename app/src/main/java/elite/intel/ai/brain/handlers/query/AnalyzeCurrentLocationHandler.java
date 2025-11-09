package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_CURRENT_PLANET;

public class AnalyzeCurrentLocationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing current location data... stand by..."));

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto location = playerSession.getCurrentLocation();
        DeathsDto deathsDto = EdsmApiClient.searchDeaths(playerSession.getPrimarySystem().getStarName());
        TrafficDto trafficDto = EdsmApiClient.searchTraffic(playerSession.getPrimarySystem().getStarName());

        return process(new AiDataStruct(ANALYZE_CURRENT_PLANET.getInstructions(), new DataDto(location, deathsDto,trafficDto)), originalUserInput);
    }

    record DataDto(LocationDto location, DeathsDto deathsData, TrafficDto trafficData) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

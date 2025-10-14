package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCurrentLocationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();

        String instructions = "Use this data to provide answers for our location. NOTE: For questions such as 'where are we?' Use planetShortName for location name unless we are on the station. If we are on a station, return station name and planet we are orbiting.";

        return analyzeData(new DataDto(playerSession.getCurrentLocation(), instructions).toJson(), originalUserInput);
    }

    record DataDto(LocationDto location, String instructions) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

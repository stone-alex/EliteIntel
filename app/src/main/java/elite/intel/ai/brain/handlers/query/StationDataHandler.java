package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

import static elite.intel.ai.brain.handlers.query.Queries.STATION_DATA;


public class StationDataHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto location = playerSession.getCurrentLocation();
        return process(new DataDto(STATION_DATA.getInstructions(), location), originalUserInput);
    }

    record DataDto(String instructions, LocationDto location) implements AiData {
        @Override public String getInstructions() {
            return instructions;
        }

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

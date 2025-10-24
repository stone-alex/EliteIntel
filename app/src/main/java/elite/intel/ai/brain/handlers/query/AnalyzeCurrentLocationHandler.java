package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

public class AnalyzeCurrentLocationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();

        String instructions = "Use this data to provide answers for our location. NOTE: For questions such as 'where are we?' Use planetShortName for location name unless we are on the station. If we are on a station, return station name and planet we are orbiting.";

        return process(new DataDto(instructions, playerSession.getCurrentLocation()), originalUserInput);
    }

    record DataDto(String instructions, LocationDto location) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

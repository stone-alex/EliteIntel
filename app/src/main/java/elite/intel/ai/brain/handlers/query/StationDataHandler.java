package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;


public class StationDataHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto location = playerSession.getCurrentLocation();

        String instructions = """
                Analyze data for the current station.
                """;

        return process(new AiDataStruct(instructions, new DataDto(location.getStationServices())), originalUserInput);
    }

    record DataDto(List<String> stationServices) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

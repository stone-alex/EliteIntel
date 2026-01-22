package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.search.edsm.EdsmApiClient;
import elite.intel.search.edsm.dto.StationsDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeStationsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        String primaryStarName = playerSession.getPrimaryStarName();
        StationsDto stationsDto = EdsmApiClient.searchStations(primaryStarName, 0);
        String instructions = """
                    Use this list of stations to answer the user's question:
                """;
        return process(new AiDataStruct(instructions, new DataDto(stationsDto)), originalUserInput);
    }

    record DataDto(StationsDto stationsDto) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

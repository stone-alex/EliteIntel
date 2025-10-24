package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.dto.ShipyardDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_LOCAL_SHIPYARD;

public class AnalyzeShipyardHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        ShipyardDto shipyard = playerSession.getCurrentLocation().getShipyard();

        return process(new DataDto(ANALYZE_LOCAL_SHIPYARD.getInstructions(), shipyard), originalUserInput);
    }

    private record DataDto(String instructions, ToJsonConvertible shipyard) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

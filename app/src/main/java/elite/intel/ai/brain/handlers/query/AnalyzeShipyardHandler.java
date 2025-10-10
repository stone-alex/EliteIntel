package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.dto.ShipyardDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeShipyardHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        ShipyardDto shipyard = playerSession.getCurrentLocation().getShipyard();

        return analyzeData(new DataDto(shipyard).toJson(), originalUserInput);
    }

    private record DataDto(ShipyardDto shipyard) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

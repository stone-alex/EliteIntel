package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;

public class AnalyzeShipyardHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object shipYardData = playerSession.get(PlayerSession.LOCAL_SHIP_YARD_JSON);

        String data = shipYardData != null ? toJson(shipYardData) : toJson(" no shipyard data available...");
        return analyzeData(data, originalUserInput);
    }
}

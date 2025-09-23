package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;

public class AnalyzeShipLoadoutHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object loadout = playerSession.get(PlayerSession.SHIP_LOADOUT);
        String data = loadout != null ? toJson(loadout) : "Carrier data is unavailable";

        return analyzeData(data, originalUserInput);
    }
}

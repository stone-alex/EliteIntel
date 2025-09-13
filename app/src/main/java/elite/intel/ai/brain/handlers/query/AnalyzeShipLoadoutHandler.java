package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class AnalyzeShipLoadoutHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Gson GSON = GsonFactory.getGson();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object loadout = playerSession.get(PlayerSession.SHIP_LOADOUT_JSON);
        String data = loadout != null ? GSON.toJson(loadout) : "Carrier data is unavailable";

        return analyzeData(data, originalUserInput);
    }
}

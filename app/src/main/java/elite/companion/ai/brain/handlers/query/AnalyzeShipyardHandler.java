package elite.companion.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.session.PlayerSession;
import elite.companion.util.json.GsonFactory;

public class AnalyzeShipyardHandler extends BaseQueryAnalyzer implements QueryHandler {
    private static final Gson GSON = GsonFactory.getGson();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object shipYardData = playerSession.get(PlayerSession.LOCAL_SHIP_YARD_JSON);

        String data = shipYardData != null ? GSON.toJson(shipYardData) : GSON.toJson(" no shipyard data available...");
        return analyzeData(data, originalUserInput);
    }
}

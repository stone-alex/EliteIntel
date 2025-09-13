package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class AnalyzeLocalMarketsHandler extends BaseQueryAnalyzer implements QueryHandler {
    private static final Gson GSON = GsonFactory.getGson();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object marketData = playerSession.get(PlayerSession.LOCAL_MARKET_JSON);

        String data = marketData != null ? GSON.toJson(marketData) : GSON.toJson(" no market data available...");
        return analyzeData(data, originalUserInput);
    }
}

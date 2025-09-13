package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class AnalyzeBountiesCollectedHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Gson GSON = GsonFactory.getGson();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        long totalBounties = playerSession.getBountyCollectedThisSession();
        return analyzeData(GSON.toJson(totalBounties), originalUserInput);
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class AnalyzeLocalOutfittingHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object outfittingData = playerSession.get(PlayerSession.LOCAL_OUTFITING_JSON);

        String data = outfittingData != null ? toJson(outfittingData) : toJson(" no outfitting data available...");
        return analyzeData(data, originalUserInput);
    }
}

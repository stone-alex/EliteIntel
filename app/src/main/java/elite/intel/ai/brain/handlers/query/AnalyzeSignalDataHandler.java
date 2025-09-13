package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class AnalyzeSignalDataHandler  extends BaseQueryAnalyzer implements QueryHandler {
    private static final Gson GSON = GsonFactory.getGson();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        Object signals = playerSession.getSignals();
        String data = signals != null ? GSON.toJson(String.valueOf(signals)) : "No data available";

        return analyzeData(data, originalUserInput);
    }
}

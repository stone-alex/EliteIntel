package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;

public class AnalyzeSignalDataHandler  extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        Object signals = playerSession.getSignals();
        String data = signals != null ? toJson(String.valueOf(signals)) : "No data available";

        return analyzeData(data, originalUserInput);
    }
}

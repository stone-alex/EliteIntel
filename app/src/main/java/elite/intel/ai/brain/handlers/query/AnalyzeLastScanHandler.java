package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class AnalyzeLastScanHandler  extends BaseQueryAnalyzer implements QueryHandler {

    private static final Gson GSON = GsonFactory.getGson();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        Object scan = playerSession.get(PlayerSession.LAST_SCAN);
        String data = scan != null ? GSON.toJson(scan) : null;

        return analyzeData(data, originalUserInput);
    }
}

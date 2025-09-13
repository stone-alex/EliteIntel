package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class AnalyzeFsdTargetHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Gson GSON = GsonFactory.getGson();


    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        Object fsdTarget = playerSession.get(PlayerSession.FSD_TARGET);

        String data = fsdTarget != null ? GSON.toJson(fsdTarget) : GSON.toJson(" no infomation available...");
        return analyzeData(data, originalUserInput);
    }
}

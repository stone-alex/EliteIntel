package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;

public class AnalyzeFsdTargetHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        Object fsdTarget = playerSession.get(PlayerSession.FSD_TARGET);

        String data = fsdTarget != null ? toJson(fsdTarget) : toJson(" no infomation available...");
        return analyzeData(data, originalUserInput);
    }
}

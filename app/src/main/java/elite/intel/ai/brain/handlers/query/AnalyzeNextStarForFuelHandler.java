package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.JsonDataFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Collection;

public class AnalyzeNextStarForFuelHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Collection<? extends ToJsonConvertible> route = playerSession.getOrderedRoute();
        String data = JsonDataFactory.getInstance().toJsonArrayString(route);
        return analyzeData(data, originalUserInput);
    }
}

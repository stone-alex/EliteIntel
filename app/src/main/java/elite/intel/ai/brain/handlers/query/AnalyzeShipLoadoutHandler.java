package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.session.PlayerSession;

public class AnalyzeShipLoadoutHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LoadoutEvent shipLoadout = playerSession.getShipLoadout();
        if(shipLoadout == null) return analyzeData(toJson("No data available"), originalUserInput);
        return analyzeData(shipLoadout.toJson(), originalUserInput);
    }
}

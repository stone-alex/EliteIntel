package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;

public class AnalyzeCarrierDataHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();

        CarrierDataDto stats = playerSession.getCarrierData();
        if (stats == null || (stats.getTotalBalance() == 0 && stats.getFuelSupply() == 0)) {
            return analyzeData(toJson("No data available. Please open carrier management panel."), originalUserInput);
        } else {
            String data = toJson(stats);
            return analyzeData(data, originalUserInput);
        }
    }
}

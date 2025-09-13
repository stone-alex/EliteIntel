package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class AnalyzeCarrierDataHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Gson GSON = GsonFactory.getGson();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();

        CarrierDataDto stats = playerSession.getCarrierData();
        String data = stats != null ? GSON.toJson(stats) : null;

        return analyzeData(data, originalUserInput);
    }
}

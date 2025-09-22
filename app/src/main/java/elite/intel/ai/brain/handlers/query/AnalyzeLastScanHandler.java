package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeLastScanHandler  extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        Object scan = playerSession.get(PlayerSession.LAST_SCAN);
        Object signals = playerSession.getCurrentLocation().getSaaSignals();
        DataDto dataDto = new DataDto(String.valueOf(scan), String.valueOf(signals));

        String data = scan != null ? dataDto.toJson() : null;

        return analyzeData(data, originalUserInput);
    }

    record DataDto(String scan, String signals) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

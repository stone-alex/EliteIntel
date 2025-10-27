package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class RemindTargetMarketStationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        StationMarket targetMarketStation = playerSession.getTargetMarketStation();

        if (targetMarketStation == null) {
            return process("No target market station is set.");
        }

        return process(new AiDataStruct("Remind the user stationName where market is located", new DataDto(targetMarketStation)), originalUserInput);
    }

    private record DataDto(StationMarket stationMarket) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

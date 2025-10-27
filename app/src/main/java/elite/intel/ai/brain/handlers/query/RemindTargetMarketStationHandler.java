package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

public class RemindTargetMarketStationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        StationMarket targetMarketStation = playerSession.getTargetMarketStation();

        if (targetMarketStation == null) {
            return process("No target market station is set.");
        }
        return process(new DataDto(targetMarketStation, "Remind the user stationName where market is located"), originalUserInput);
    }

    private record DataDto(StationMarket stationMarket, String instructions) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

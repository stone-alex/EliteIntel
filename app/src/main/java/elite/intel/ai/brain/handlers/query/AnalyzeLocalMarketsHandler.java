package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

import java.util.List;

public class AnalyzeLocalMarketsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        List<StationMarket> markets = playerSession.getMarkets();
        return process(new DataDto("Use markets data to provide answers.", markets), originalUserInput);
    }

    private record DataDto(String instructions, List<StationMarket> markets) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

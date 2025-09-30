package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class AnalyzeLocalMarketsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        List<StationMarket> markets = playerSession.getMarkets();
        return analyzeData(new DataDto(markets).toJson(), originalUserInput);
    }

    private record DataDto(List<StationMarket> markets) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class AnalyzeLocalMarketsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing local market data... stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        List<StationMarket> markets = playerSession.getMarkets();
        return process(new AiDataStruct("Use markets data to provide answers.", new DataDto(markets)), originalUserInput);
    }

    private record DataDto(List<StationMarket> markets) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.search.spansh.station.marketstation.TradeStopDto;

import java.util.List;

public class AnalyzeTradeRouteHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        TradeRouteManager tradeRouteManager = TradeRouteManager.getInstance();
        List<TradeRouteManager.TradeRouteLegTuple<Integer, TradeStopDto>> allStops = tradeRouteManager.getAllStops();

        StringBuilder sb = new StringBuilder();
        for (TradeRouteManager.TradeRouteLegTuple<Integer, TradeStopDto> tuple : allStops) {
            sb.append("Leg: ");
            sb.append(tuple.getLegNumber())
                    .append(", Source System: ")
                    .append(tuple.getTradeStopDto().getSourceSystem())
                    .append(", Station: ")
                    .append(tuple.getTradeStopDto().getSourceStation())
                    .append(", Commodities: ")
                    .append(tuple.getTradeStopDto().getCommodities().stream().collect(() -> new StringBuilder(), (sb1, commodity) -> sb1.append(commodity.getName()).append(", "), StringBuilder::append))
                    .append(", Destination System: ")
                    .append(tuple.getTradeStopDto().getDestinationSystem())
                    .append(", Destination Station: ")
                    .append(tuple.getTradeStopDto().getDestinationStation())
                    .append("\n");
        }
        return process(sb.toString());
    }
}

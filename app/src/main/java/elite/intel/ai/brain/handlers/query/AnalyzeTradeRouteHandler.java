package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.search.spansh.station.marketstation.TradeStopDto;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.List;

public class AnalyzeTradeRouteHandler extends BaseQueryAnalyzer implements QueryHandler {

    public static final String INSTRUCTIONS = """ 
            Use this data to answer users' queries
            """;

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
        if(allStops.isEmpty()){
            return process("No trade route legs found");
        }
        return process(new AiDataStruct(INSTRUCTIONS, new DataDto(sb.toString())), originalUserInput);
    }

    record DataDto(String data) implements ToYamlConvertable {

        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

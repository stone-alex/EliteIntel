package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.search.spansh.traderoute.TradeRouteSearchCriteria;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_TRADE_PROFILE;

public class WhatIsMyCurrentTradeProfileHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        TradeProfileManager tradeProfileManager = TradeProfileManager.getInstance();
        TradeRouteSearchCriteria criteria = tradeProfileManager.getCriteria();
        return process(
                new AiDataStruct(
                        ANALYZE_TRADE_PROFILE.getInstructions(),
                        new DataDto(criteria.getQuery())
                ),
                originalUserInput
        );
    }

    record DataDto(String profileData) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

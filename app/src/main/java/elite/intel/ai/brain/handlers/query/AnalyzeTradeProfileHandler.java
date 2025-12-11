package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.traderoute.TradeRouteSearchCriteria;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeTradeProfileHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing trade profile... Stand by..."));
        TradeProfileManager tradeProfileManager = TradeProfileManager.getInstance();
        TradeRouteSearchCriteria criteria = tradeProfileManager.getCriteria(false);
        String instructions = "Summarize the trade profile for the player in plain English.  \n" +
                "- Convert any price age given in seconds to \"X hours\" or \"X hours Y minutes\".  \n" +
                "- If the startingBudget is 0 the profile is not configured yet.  \n" +
                "- The value maxStationDistanceLs (or maxLsFromArrival) is the maximum allowed station distance **in Light Seconds from the arrival point / main star** — it is an in-system distance, never light-years and never a distance between systems.  \n" +
                "- Example: 6000 → \"stations no farther than 6000 Ls from the star\" or \"max 6000 Ls from arrival\".  \n" +
                "- Hops are measured in light years. (distances between stars)  \n" +
                "- allowStrongHold field is true/false - allow or dissalow stops at enemy strongholds for power players. Mention state of allowStrongHold or not\n" +
                "- Completely omit the system name and station name.";
        return process(
                new AiDataStruct(
                        instructions,
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

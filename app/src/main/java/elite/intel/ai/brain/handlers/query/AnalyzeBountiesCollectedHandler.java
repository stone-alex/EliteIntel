package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeBountiesCollectedHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        long totalBounties = playerSession.getBountyCollectedThisSession();

        return analyzeData(new DataDto(totalBounties, "Return total amount of bounties collected.").toJson(), originalUserInput);
    }

    record DataDto(long totalBounties, String instruction) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

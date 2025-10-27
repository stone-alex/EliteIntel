package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.TOTAL_BOUNTIES_COLLECTED;

public class AnalyzeBountiesCollectedHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        long totalBounties = playerSession.getBountyCollectedThisSession();

        return process(new AiDataStruct(TOTAL_BOUNTIES_COLLECTED.getInstructions(), new DataDto(totalBounties)), originalUserInput);
    }

    record DataDto(long totalBounties) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

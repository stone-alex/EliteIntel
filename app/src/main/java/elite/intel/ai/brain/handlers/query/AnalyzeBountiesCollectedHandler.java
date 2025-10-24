package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

import static elite.intel.ai.brain.handlers.query.Queries.TOTAL_BOUNTIES_COLLECTED;

public class AnalyzeBountiesCollectedHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        long totalBounties = playerSession.getBountyCollectedThisSession();

        return process(new DataDto(TOTAL_BOUNTIES_COLLECTED.getInstructions(), totalBounties), originalUserInput);
    }

    record DataDto(String instructions, long totalBounties) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

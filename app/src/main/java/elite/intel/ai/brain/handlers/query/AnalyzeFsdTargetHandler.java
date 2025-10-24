package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

public class AnalyzeFsdTargetHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        String fsdTarget = playerSession.getFsdTarget();
        String data = fsdTarget != null ? toJson(fsdTarget) : toJson(" no information available...");
        String instructions = "Use this data to provide answers for the currently selected FSD target";
        return process(new DataDto(instructions, data), originalUserInput);
    }

    record DataDto(String instructions, String data) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }

}

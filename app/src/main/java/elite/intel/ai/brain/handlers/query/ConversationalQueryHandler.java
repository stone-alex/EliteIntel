package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.util.json.GsonFactory;

public class ConversationalQueryHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        String instruction = "If 'User Input' field in Data is null or empty string: respond with '[summarize key data from params]'. Else: respond conversationally to the User Input.";

        return process(new DataDto(instruction, params), originalUserInput);
    }

    record DataDto(String instructions, JsonObject params) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}
package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class ConversationalQueryHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        return process(
                new AiDataStruct(
                        "If 'User Input' field in Data is null or empty string: respond with '[summarize key data from params]'. Else: respond conversationally to the User Input.",
                        new DataDto(params)
                ),
                originalUserInput
        );
    }

    record DataDto(JsonObject params) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
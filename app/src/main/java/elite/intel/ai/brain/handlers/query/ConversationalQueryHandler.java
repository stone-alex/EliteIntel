package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class ConversationalQueryHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        return analyzeData(new DataDto("Answer using your general knowledge").toJson(), originalUserInput);
    }

    record DataDto(String instruction) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
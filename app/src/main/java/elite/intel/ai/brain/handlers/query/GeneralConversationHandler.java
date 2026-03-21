package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class GeneralConversationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        String instructions = """
                Respond naturally to the user's message using your own knowledge.
                """;
        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto()
                ),
                originalUserInput
        );
    }

    record DataDto() implements ToYamlConvertable {
        /// blank
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
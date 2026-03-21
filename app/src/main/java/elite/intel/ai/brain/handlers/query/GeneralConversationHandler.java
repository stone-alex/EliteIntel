package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.session.ChatHistory;
import elite.intel.session.SystemSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class GeneralConversationHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final SystemSession systemSession = SystemSession.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        if (!systemSession.useLocalQueryLlm()) {

            ChatHistory chatHistory = systemSession.getChatHistory();
            String instructions = """
                    Respond naturally to the user's message using your own knowledge.
                    
                    Data fields:
                    - chatHistory.commanderLog: what the user said in previous turns
                    - chatHistory.shipBrief: what you said in previous turns
                    
                    These fields are silent context only. Use them to inform your understanding of the conversation.
                    Do not reference, repeat, summarize, or acknowledge them in your response.
                    """;
            return process(
                    new AiDataStruct(
                            instructions,
                            new DataDto(chatHistory)
                    ),
                    originalUserInput
            );
        } else {
            return process(new AiDataStruct("Respond naturally to the user's message using your own knowledge", new TextData(originalUserInput)), originalUserInput);
        }

    }

    record TextData(String text) implements ToYamlConvertable {
        @Override
        public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    record DataDto(ToYamlConvertable chatHistory) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
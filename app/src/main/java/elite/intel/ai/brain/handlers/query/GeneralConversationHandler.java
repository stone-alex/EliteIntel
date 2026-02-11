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

        ChatHistory chatHistory = systemSession.getChatHistory();
        String instructions = """
        General Chat.
        Use your own knowledge to chat with user.
        
        You are also provided with chat history for your own reference.
            - commanderLog is what user said in previous conversation.
            - coPilotBrief is what you said in previous conversation.
        """;
        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(chatHistory)
                ),
                originalUserInput
        );
    }

    record DataDto(ToYamlConvertable chatHistory) implements ToYamlConvertable {
        /// blank
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
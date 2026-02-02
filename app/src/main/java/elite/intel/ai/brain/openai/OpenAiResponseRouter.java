package elite.intel.ai.brain.openai;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AIRouterInterface;
import elite.intel.ai.brain.commons.ResponseRouter;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

import static elite.intel.ai.brain.handlers.query.Queries.GENERAL_CONVERSATION;

public class OpenAiResponseRouter extends ResponseRouter implements AIRouterInterface {

    private static final Logger log = LogManager.getLogger(OpenAiResponseRouter.class);

    private static OpenAiResponseRouter instance;

    private OpenAiResponseRouter() {
        // signeton pattern
    }

    public static synchronized OpenAiResponseRouter getInstance() {
        if (instance == null) {
            instance = new OpenAiResponseRouter();
        }
        return instance;
    }

    @Override public void processAiResponse(JsonObject jsonResponse, @Nullable String userInput) {
        if (jsonResponse == null) {
            log.error("Null Open AI response received");
            return;
        }
        try {
            String type = getAsStringOrEmpty(jsonResponse, "type").toLowerCase();
            String responseText = getAsStringOrEmpty(jsonResponse, AIConstants.PROPERTY_RESPONSE_TEXT);
            String action = getAsStringOrEmpty(jsonResponse, AIConstants.TYPE_ACTION);
            JsonObject params = getAsObjectOrEmpty(jsonResponse, "params");

            if (!responseText.isEmpty() && !type.equals(AIConstants.TYPE_CHAT)) {
                EventBusManager.publish(new AiVoxResponseEvent(responseText));
                log.info("Spoke initial response: {}", responseText);
            }
            EventBusManager.publish(new AppLogEvent("\nCHAT GPT LLM Action: " + action));
            switch (type) {
                case AIConstants.TYPE_COMMAND:
                    handleCommand(action, params, responseText);
                    break;
                case AIConstants.TYPE_QUERY:
                    handleQuery(action, params, userInput);
                    break;
                case AIConstants.TYPE_CHAT:
                    params.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, responseText);
                    handleQuery(GENERAL_CONVERSATION.getAction(), params, userInput);
                    break;
                default:
                    log.warn("Unknown or missing response type: '{}'", type);
                    handleChat("I'm not sure what you meant. Please try again.");
            }
        } catch (Exception e) {
            log.error("Failed to process Open AI response: {}", e.getMessage(), e);
            handleChat("Error processing response.");
        } finally {
            EventBusManager.publish(new AppLogEvent("\n"));
        }
    }

    private void handleQuery(String action, JsonObject params, String userInput) {
        QueryHandler handler = getQueryHandlers().get(action);
        if (handler == null || action == null || action.isEmpty()) {
            handler = getQueryHandlers().get(GENERAL_CONVERSATION.getAction());
            action = GENERAL_CONVERSATION.getAction();
            log.info("No specific query handler found, routing to general_conversation");
        }

        try {
            JsonObject dataJson = handler.handle(action, params, userInput);
            if (dataJson == null) return;
            String responseTextToUse = dataJson.has(AIConstants.PROPERTY_RESPONSE_TEXT) ? dataJson.get(AIConstants.PROPERTY_RESPONSE_TEXT).getAsString() : "";
            if (responseTextToUse != null && !responseTextToUse.isEmpty()) {
                EventBusManager.publish(new AiVoxResponseEvent(responseTextToUse));
                log.info("Spoke final query response (action: {}): {}", action, responseTextToUse);
            }
        } catch (Exception e) {
            log.error("Query handling failed for action {}: {}", action, e.getMessage(), e);
            handleChat("Error accessing data banks: ");
        } finally {
            SystemSession.getInstance().clearChatHistory();
        }
    }
}
package elite.intel.ai.brain.openai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AIRouterInterface;
import elite.intel.ai.brain.AiContextFactory;
import elite.intel.ai.brain.AiQueryInterface;
import elite.intel.ai.brain.commons.ResponseRouter;
import elite.intel.ai.brain.handlers.query.QueryActions;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.session.SystemSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class OpenAiResponseRouter extends ResponseRouter implements AIRouterInterface {
    private static final Logger log = LogManager.getLogger(OpenAiResponseRouter.class);
    private static OpenAiResponseRouter instance;
    private final AiQueryInterface queryInterface;
    private final AiContextFactory contextFactory;

    public static synchronized OpenAiResponseRouter getInstance() {
        if (instance == null) {
            instance = new OpenAiResponseRouter();
        }
        return instance;
    }

    private OpenAiResponseRouter() {
        try {
            this.queryInterface = ApiFactory.getInstance().getQueryEndpoint();
            this.contextFactory = ApiFactory.getInstance().getAiContextFactory();
        } catch (Exception e) {
            log.error("Failed to initialize OpenAiResponseRouter", e);
            throw new RuntimeException("OpenAiResponseRouter initialization failed", e);
        }
    }

    @Override
    public void processAiResponse(JsonObject jsonResponse, @Nullable String userInput) {
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
                EventBusManager.publish(new VocalisationRequestEvent(responseText));
                log.info("Spoke initial response: {}", responseText);
            }

            switch (type) {
                case AIConstants.TYPE_COMMAND:
                    handleCommand(action, params, responseText, jsonResponse);
                    break;
                case AIConstants.TYPE_QUERY:
                    handleQuery(action, params, userInput);
                    break;
                case AIConstants.TYPE_CHAT:
                    handleChat(responseText);
                    break;
                default:
                    log.warn("Unknown or missing response type: '{}'", type);
                    handleChat("I'm not sure what you meant. Please try again.");
            }
        } catch (Exception e) {
            log.error("Failed to process Open AI response: {}", e.getMessage(), e);
            handleChat("Error processing response.");
        }
    }

    private void handleQuery(String action, JsonObject params, String userInput) {
        QueryHandler handler = getQueryHandlers().get(action);
        if (handler == null || action == null || action.isEmpty()) {
            handler = getQueryHandlers().get(QueryActions.GENERAL_CONVERSATION.getAction());
            action = QueryActions.GENERAL_CONVERSATION.getAction();
            log.info("No specific query handler found, routing to general_conversation");
        }

        try {
            JsonObject dataJson = handler.handle(action, params, userInput);
            String responseTextToUse = dataJson.has(AIConstants.PROPERTY_RESPONSE_TEXT)
                    ? dataJson.get(AIConstants.PROPERTY_RESPONSE_TEXT).getAsString()
                    : "";
            boolean requiresFollowUp = dataJson.has(AIConstants.PROPERTY_EXPECT_FOLLOWUP)
                    ? dataJson.get(AIConstants.PROPERTY_EXPECT_FOLLOWUP).getAsBoolean()
                    : false;

            // Override requiresFollowUp from QueryActions to ensure consistency
            for (QueryActions qa : QueryActions.values()) {
                if (qa.getAction().equals(action)) {
                    requiresFollowUp = qa.isRequiresFollowUp();
                    break;
                }
            }

            if (responseTextToUse != null && !responseTextToUse.isEmpty()) {
                EventBusManager.publish(new VocalisationRequestEvent(responseTextToUse));
                SystemSession.getInstance().clearChatHistory();
                log.info("Spoke final query response (action: {}): {}", action, responseTextToUse);
            } else if (requiresFollowUp) {
                JsonArray messages = new JsonArray();
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", AIConstants.ROLE_SYSTEM);
                systemMessage.addProperty("content", contextFactory.generateQueryPrompt());
                messages.add(systemMessage);

                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", AIConstants.ROLE_USER);
                String queryContent = dataJson.has(AIConstants.PROPERTY_ORIGINAL_QUERY)
                        ? dataJson.get(AIConstants.PROPERTY_ORIGINAL_QUERY).getAsString()
                        : userInput;
                userMessage.addProperty("content", queryContent);
                messages.add(userMessage);

                JsonObject toolResult = new JsonObject();
                toolResult.addProperty("role", AIConstants.ROLE_TOOL);
                toolResult.addProperty("name", action);
                toolResult.addProperty("content", "Query Action: " + action + "\nResponse Text: " + responseTextToUse +
                        "\nInstructions: Set 'type' to 'chat', generate a response using general knowledge for 'general_conversation', set 'action' to null, 'params' to {}, and 'expect_followup' to false.");
                messages.add(toolResult);

                log.debug("Sending follow-up to OpenAiQueryEndPoint for action: {}", action);
                SystemSession.getInstance().appendToChatHistory(userMessage, systemMessage);
                JsonObject followUpResponse = queryInterface.sendToAi(messages);

                if (followUpResponse == null) {
                    log.warn("Follow-up response is null for action: {}", action);
                    handleChat("Error accessing data banks.");
                    return;
                }

                String finalResponseText = getAsStringOrEmpty(followUpResponse, "response_text");
                if (!finalResponseText.isEmpty()) {
                    EventBusManager.publish(new VocalisationRequestEvent(finalResponseText));
                    log.info("Spoke follow-up query response (action: {}): {}", action, finalResponseText);
                } else {
                    log.warn("No response_text in follow-up for action: {}", action);
                    handleChat("Error accessing data banks.");
                }
            } else {
                log.warn("No response_text for action: {}, and no follow-up required", action);
                handleChat("No data available for that query.");
            }
        } catch (Exception e) {
            log.error("Query handling failed for action {}: {}", action, e.getMessage(), e);
            handleChat("Error accessing data banks: " + e.getMessage());
        }
    }
}
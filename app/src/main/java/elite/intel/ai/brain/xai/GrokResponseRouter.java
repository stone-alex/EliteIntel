package elite.intel.ai.brain.xai;

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

import static elite.intel.ai.brain.handlers.query.QueryActions.GENERAL_CONVERSATION;

/**
 * GrokResponseRouter acts as a central router for handling AI responses, commands, and queries.
 * It processes input from AI services and delegates the execution to specific handlers based on
 * the type of response received.
 * <p>
 * This class ensures modularity in handling a variety of commands and queries by dynamically
 * registering and invoking command and query handlers. It also integrates with external systems
 * and supports features like follow-up actions and context management as part of its operation.
 * <p>
 * GrokResponseRouter implements the AIRouterInterface, thereby defining the lifecycle methods
 * for managing the router's operation and response processing.
 */
public class GrokResponseRouter extends ResponseRouter implements AIRouterInterface {
    private static final Logger log = LogManager.getLogger(GrokResponseRouter.class);
    private static final GrokResponseRouter INSTANCE = new GrokResponseRouter();
    private final AiQueryInterface queryInterface;
    private final AiContextFactory contextFactory;

    public static GrokResponseRouter getInstance() {
        return INSTANCE;
    }

    private GrokResponseRouter() {
        try {
            this.queryInterface = ApiFactory.getInstance().getQueryEndpoint();
            this.contextFactory = ApiFactory.getInstance().getAiContextFactory();
        } catch (Exception e) {
            log.error("Failed to initialize GrokResponseRouter", e);
            throw new RuntimeException("GrokResponseRouter initialization failed", e);
        }
    }


    @Override public void processAiResponse(JsonObject jsonResponse, @Nullable String userInput) {
        if (jsonResponse == null) {
            log.error("Null Grok response received");
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
            log.error("Failed to process Grok response: {}", e.getMessage(), e);
            handleChat("Error processing response.");
        }
    }

    /**
     * Handles the processing of a given query based on the specified action, parameters,
     * and user input. Routes the query to the appropriate query handler and manages
     * the response, including follow-up actions if required.
     *
     * @param action    The action identifier for the query, used to determine which query handler to invoke.
     *                  If null or empty, it defaults to "general_conversation".
     * @param params    The parameters associated with the query to provide additional context or data for processing.
     * @param userInput The original user input prompting the query, used as a fallback in certain processing paths.
     */
    private void handleQuery(String action, JsonObject params, String userInput) {
        QueryHandler handler = getQueryHandlers().get(action);
        if (handler == null || action == null || action.isEmpty()) {
            handler = getQueryHandlers().get(GENERAL_CONVERSATION.getAction());
            action = GENERAL_CONVERSATION.getAction();
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

            if (!requiresFollowUp && responseTextToUse != null && !responseTextToUse.isEmpty()) {
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

                log.debug("Sending follow-up to GrokQueryEndPoint for action: {}", action);
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
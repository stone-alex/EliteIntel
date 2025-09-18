package elite.intel.ai.brain.xai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIRouterInterface;
import elite.intel.ai.brain.AiContextFactory;
import elite.intel.ai.brain.AiQueryInterface;
import elite.intel.ai.brain.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.handlers.QueryHandlerFactory;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.query.QueryActions;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.ui.event.AppLogEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import javax.annotation.Nullable;
import java.util.Map;

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
public class GrokResponseRouter implements AIRouterInterface {
    private static final Logger log = LogManager.getLogger(GrokResponseRouter.class);
    private static final GrokResponseRouter INSTANCE = new GrokResponseRouter();
    private final Map<String, CommandHandler> commandHandlers;
    private final Map<String, QueryHandler> queryHandlers;
    private final AiQueryInterface queryInterface;
    private final AiContextFactory contextFactory;

    public static GrokResponseRouter getInstance() {
        return INSTANCE;
    }

    private GrokResponseRouter() {
        try {
            CommandHandlerFactory commandHandlerFactory = CommandHandlerFactory.getInstance();
            this.commandHandlers = commandHandlerFactory.registerCommandHandlers();
            this.queryHandlers = QueryHandlerFactory.getInstance().registerQueryHandlers();
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
            String responseText = getAsStringOrEmpty(jsonResponse, "response_text");
            String action = getAsStringOrEmpty(jsonResponse, "action");
            JsonObject params = getAsObjectOrEmpty(jsonResponse, "params");

            if (!responseText.isEmpty() && !type.equals("chat")) {
                EventBusManager.publish(new VoiceProcessEvent(responseText));
                log.info("Spoke initial response: {}", responseText);
            }

            switch (type) {
                case "command":
                    handleCommand(action, params, responseText, jsonResponse);
                    break;
                case "query":
                    handleQuery(action, params, userInput);
                    break;
                case "chat":
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
        QueryHandler handler = queryHandlers.get(action);
        if (handler == null || action == null || action.isEmpty()) {
            handler = queryHandlers.get(GENERAL_CONVERSATION.getAction());
            action = GENERAL_CONVERSATION.getAction();
            log.info("No specific query handler found, routing to general_conversation");
        }

        try {
            JsonObject dataJson = handler.handle(action, params, userInput);
            String responseTextToUse = dataJson.has("response_text")
                    ? dataJson.get("response_text").getAsString()
                    : "";
            boolean requiresFollowUp = dataJson.has("expect_followup")
                    ? dataJson.get("expect_followup").getAsBoolean()
                    : false;

            // Override requiresFollowUp from QueryActions to ensure consistency
            for (QueryActions qa : QueryActions.values()) {
                if (qa.getAction().equals(action)) {
                    requiresFollowUp = qa.isRequiresFollowUp();
                    break;
                }
            }

            if (responseTextToUse != null && !responseTextToUse.isEmpty()) {
                EventBusManager.publish(new VoiceProcessEvent(responseTextToUse));
                log.info("Spoke final query response (action: {}): {}", action, responseTextToUse);
            } else if (requiresFollowUp) {
                JsonArray messages = new JsonArray();
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", contextFactory.generateQueryPrompt());
                messages.add(systemMessage);

                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", "user");
                String queryContent = dataJson.has("original_query")
                        ? dataJson.get("original_query").getAsString()
                        : userInput;
                userMessage.addProperty("content", queryContent);
                messages.add(userMessage);

                JsonObject toolResult = new JsonObject();
                toolResult.addProperty("role", "tool");
                toolResult.addProperty("name", action);
                toolResult.addProperty("content", "Query Action: " + action + "\nResponse Text: " + responseTextToUse +
                        "\nInstructions: Set 'type' to 'chat', generate a response using general knowledge for 'general_conversation', set 'action' to null, 'params' to {}, and 'expect_followup' to false.");
                messages.add(toolResult);

                log.debug("Sending follow-up to GrokQueryEndPoint for action: {}", action);
                JsonObject followUpResponse = queryInterface.sendToAi(messages);

                if (followUpResponse == null) {
                    log.warn("Follow-up response is null for action: {}", action);
                    handleChat("Error accessing data banks.");
                    return;
                }

                String finalResponseText = getAsStringOrEmpty(followUpResponse, "response_text");
                if (!finalResponseText.isEmpty()) {
                    EventBusManager.publish(new VoiceProcessEvent(finalResponseText));
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

    private void handleCommand(String action, JsonObject params, String responseText, JsonObject jsonResponse) {
        EventBusManager.publish(new AppLogEvent("DEBUG: Processing action: " + action + " with params: " + params.toString()));
        CommandHandler handler = commandHandlers.get(action);
        if (handler != null) {
            EventBusManager.publish(new AppLogEvent("DEBUG: Command handler: " + handler.getClass().getSimpleName()));
            handler.handle(params, responseText);
            log.debug("Handled command action: {}", action);
        }
    }

    private void handleChat(String responseText) {
        if (!responseText.isEmpty()) {
            EventBusManager.publish(new VoiceProcessEvent(responseText));
            log.info("Sent to VoiceGenerator: {}", responseText);
        }
    }

    private static String getAsStringOrEmpty(JsonObject obj, String key) {
        if (obj == null || key == null) return "";
        if (!obj.has(key)) return "";
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return "";
        if (el.isJsonPrimitive()) {
            try {
                return el.getAsString();
            } catch (UnsupportedOperationException ignored) {
                // fallthrough
            }
        }
        log.debug("Expected string for key '{}' but got {}", key, el);
        return "";
    }

    private static JsonObject getAsObjectOrEmpty(JsonObject obj, String key) {
        if (obj == null || key == null) return new JsonObject();
        if (!obj.has(key)) return new JsonObject();
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return new JsonObject();
        if (el.isJsonObject()) return el.getAsJsonObject();
        log.debug("Expected object for key '{}' but got {}", key, el);
        return new JsonObject();
    }
}
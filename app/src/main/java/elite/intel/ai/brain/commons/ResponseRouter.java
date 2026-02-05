package elite.intel.ai.brain.commons;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AIRouterInterface;
import elite.intel.ai.brain.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.handlers.QueryHandlerFactory;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.StringUtls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

import static elite.intel.ai.brain.handlers.query.Queries.GENERAL_CONVERSATION;
import static elite.intel.util.json.JsonUtils.nullSaveJsonObject;


public class ResponseRouter implements AIRouterInterface {

    private static final Logger log = LogManager.getLogger(ResponseRouter.class);
    private static final ResponseRouter INSTANCE = new ResponseRouter();
    private final Map<String, CommandHandler> commandHandlers;
    private final Map<String, QueryHandler> queryHandlers;
    private final SystemSession systemSession;
    private final PlayerSession playerSession;

    private ResponseRouter() {
        try {
            commandHandlers = CommandHandlerFactory.getInstance().registerCommandHandlers();
            queryHandlers = QueryHandlerFactory.getInstance().registerQueryHandlers();
            this.systemSession = SystemSession.getInstance();
            this.playerSession = PlayerSession.getInstance();
        } catch (Exception e) {
            log.error("Failed to initialize ResponseRouter", e);
            throw new RuntimeException("ResponseRouter initialization failed", e);
        }
    }

    public static ResponseRouter getInstance() {
        return INSTANCE;
    }

    @Override public void processAiResponse(JsonObject jsonResponse, @Nullable String userInput) {
        if (jsonResponse == null) {
            log.error("Null LLM response received");
            return;
        }
        try {
            String type = getAsStringOrEmpty(jsonResponse, "type").toLowerCase();
            String responseText = getAsStringOrEmpty(jsonResponse, AIConstants.PROPERTY_RESPONSE_TEXT);
            String action = getAsStringOrEmpty(jsonResponse, AIConstants.TYPE_ACTION);
            JsonObject params = getAsObjectOrEmpty(jsonResponse);

            if (action == null) {
                type = AIConstants.TYPE_CHAT;
            }

            if (!responseText.isEmpty() && type.equals(AIConstants.TYPE_CHAT)) {
                EventBusManager.publish(new AiVoxResponseEvent(responseText));
                log.info("Spoke initial response: {}", responseText);
                return;
            }

            String paramsForLogging = action + (params == null ? "" : " params " + params);
            if (systemSession.useLocalCommandLlm()) {
                EventBusManager.publish(new AppLogEvent("\nLocal LLM Action: " + paramsForLogging));
            } else {
                EventBusManager.publish(new AppLogEvent("\nCloud LLM Action: " + paramsForLogging));
            }

            switch (type) {
                case AIConstants.TYPE_COMMAND:
                    handleCommand(action, params, responseText);
                    break;
                case AIConstants.TYPE_QUERY:
                    handleQuery(action, params, userInput);
                    break;
                default:
                    handleChat(responseText);
            }
        } catch (Exception e) {
            log.error("Failed to process LLM response: {}", e.getMessage(), e);
            EventBusManager.publish(new AiVoxResponseEvent("Error processing response."));
        } finally {
            EventBusManager.publish(new AppLogEvent("\n"));
        }
    }

    private void handleQuery(String action, JsonObject params, String userInput) {
        QueryHandler handler = getQueryHandlers().get(action);
        if (handler == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("command not found"));
            return;
        }

        EventBusManager.publish(new AppLogEvent("Query handler: " + handler.getClass().getSimpleName()));
        if (action == null || action.isEmpty()) {
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
                systemSession.clearChatHistory();
                log.info("Spoke final query response (action: {}): {}", action, responseTextToUse);
            }
        } catch (Exception e) {
            log.error("Query handling failed for action {}: {}", action, e.getMessage(), e);
            handleChat("Error processing request");
        } finally {
            systemSession.clearChatHistory();
        }
    }


    protected Map<String, CommandHandler> getCommandHandlers() {
        return commandHandlers;
    }

    protected Map<String, QueryHandler> getQueryHandlers() {
        return queryHandlers;
    }


    protected void handleChat(String responseText) {
        if (!responseText.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent(responseText));
            log.info("Sent to VoiceGenerator: {}", responseText);
        }
    }

    protected void handleCommand(String action, JsonObject params, String responseText) {
        EventBusManager.publish(new AppLogEvent("Processing action: " + action + " with params: " + params.toString()));
        if(!"verify_llm_connection_command".equalsIgnoreCase(action)){
            EventBusManager.publish(new AiVoxResponseEvent("%s, %s ".formatted(StringUtls.affirmative(), StringUtls.player(playerSession))));
        }

        CommandHandler handler = getCommandHandlers().get(action);
        if (handler == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("command not found"));
            return;
        }

        EventBusManager.publish(new AppLogEvent("Command handler: " + handler.getClass().getSimpleName()));
        new Thread(() -> handler.handle(action, params, responseText)).start();
        log.debug("Handled command action: {}", action);
        systemSession.clearChatHistory();
    }


    protected String getAsStringOrEmpty(JsonObject obj, String key) {
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

    protected JsonObject getAsObjectOrEmpty(JsonObject obj) {
        return nullSaveJsonObject(obj, AIConstants.PARAMS, log);
    }

}

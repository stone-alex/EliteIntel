package elite.intel.ai.brain.commons;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.handlers.QueryHandlerFactory;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public abstract class ResponseRouter  {

    private static final Logger log = LogManager.getLogger(ResponseRouter.class);
    private final SystemSession systemSession = SystemSession.getInstance();

    protected ResponseRouter() {
        commandHandlers =  CommandHandlerFactory.getInstance().registerCommandHandlers();
        queryHandlers = QueryHandlerFactory.getInstance().registerQueryHandlers();;
    }

    private final Map<String, CommandHandler> commandHandlers;
    private final Map<String, QueryHandler> queryHandlers;


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
        EventBusManager.publish(new AppLogEvent("DEBUG: Processing action: " + action + " with params: " + params.toString()));
        CommandHandler handler = getCommandHandlers().get(action.replace("action:", ""));
        if (handler != null) {
            EventBusManager.publish(new AppLogEvent("DEBUG: Command handler: " + handler.getClass().getSimpleName()));
            new Thread(() -> handler.handle(action, params, responseText)).start();
            log.debug("Handled command action: {}", action);
            systemSession.clearChatHistory();
        }
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

    protected JsonObject getAsObjectOrEmpty(JsonObject obj, String key) {
        if (obj == null || key == null) return new JsonObject();
        if (!obj.has(key)) return new JsonObject();
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return new JsonObject();
        if (el.isJsonObject()) return el.getAsJsonObject();
        log.debug("Expected object for key '{}' but got {}", key, el);
        return new JsonObject();
    }
}

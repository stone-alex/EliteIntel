package elite.companion.comms.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.companion.comms.ai.robot.GameCommandHandler;
import elite.companion.comms.handlers.command.*;
import elite.companion.comms.handlers.query.*;
import elite.companion.comms.voice.VoiceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class GrokResponseRouter {
    private static final Logger log = LoggerFactory.getLogger(GrokResponseRouter.class);
    private static final GrokResponseRouter INSTANCE = new GrokResponseRouter();
    private final GameCommandHandler _gameCommandHandler;
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private final Map<String, QueryHandler> queryHandlers = new HashMap<>();

    public static GrokResponseRouter getInstance() {
        return INSTANCE;
    }

    private GrokResponseRouter() {
        try {
            this._gameCommandHandler = new GameCommandHandler();
            registerCommandHandlers();
        } catch (Exception e) {
            log.error("Failed to initialize GrokResponseRouter", e);
            throw new RuntimeException("GrokResponseRouter initialization failed", e);
        }
    }

    private void registerCommandHandlers() {
        for (QueryActions action : QueryActions.values()) {
            try {
                QueryHandler handler = instantiateHandler(action.getHandlerClass(), QueryHandler.class);
                queryHandlers.put(action.getAction(), handler);
                log.debug("Registered query handler for action: {}, requiresFollowUp: {}", action.getAction(), action.isRequiresFollowUp());
            } catch (Exception e) {
                log.error("Failed to register query handler for action: {}", action.getAction(), e);
                throw new RuntimeException("Query handler registration failed for action: " + action.getAction(), e);
            }
        }

        for (CommandActionsCustom action : CommandActionsCustom.values()) {
            try {
                CommandHandler handler = instantiateCommandHandler(action.getHandlerClass(), action.getAction());
                commandHandlers.put(action.getAction(), handler);
                log.debug("Registered custom command handler for action: {}", action.getAction());
            } catch (Exception e) {
                log.error("Failed to register custom command handler for action: {}", action.getAction(), e);
                throw new RuntimeException("Custom command handler registration failed for action: " + action.getAction(), e);
            }
        }

        for (CommandActionsGame.GameCommand command : CommandActionsGame.GameCommand.values()) {
            try {
                CommandHandler handler = instantiateCommandHandler(command.getHandlerClass(), command.getGameBinding());
                commandHandlers.put(command.getGameBinding(), handler);
                log.debug("Registered game command handler for binding: {}", command.getGameBinding());
            } catch (Exception e) {
                log.error("Failed to register game command handler for binding: {}", command.getGameBinding(), e);
                throw new RuntimeException("Game command handler registration failed for binding: " + command.getGameBinding(), e);
            }
        }
    }

    private <T> T instantiateHandler(Class<? extends T> handlerClass, Class<T> expectedType) {
        try {
            Constructor<? extends T> constructor = handlerClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T handler = constructor.newInstance();
            if (!expectedType.isInstance(handler)) {
                throw new IllegalStateException("Handler class " + handlerClass.getName() + " does not implement " + expectedType.getName());
            }
            return handler;
        } catch (NoSuchMethodException e) {
            log.error("No no-arg constructor found for handler: {}", handlerClass.getName());
            throw new RuntimeException("Failed to instantiate handler: " + handlerClass.getName(), e);
        } catch (Exception e) {
            log.error("Failed to instantiate handler: {}", handlerClass.getName(), e);
            throw new RuntimeException("Failed to instantiate handler: " + handlerClass.getName(), e);
        }
    }

    private CommandHandler instantiateCommandHandler(Class<? extends CommandHandler> handlerClass, String actionOrBinding) {
        try {
            if (handlerClass == GenericGameController.class) {
                Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor(GameCommandHandler.class, String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(_gameCommandHandler, actionOrBinding);
            } else if (handlerClass == SetRouteHandler.class) {
                Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor(GameCommandHandler.class);
                constructor.setAccessible(true);
                return constructor.newInstance(_gameCommandHandler);
            } else {
                try {
                    Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor(GameCommandHandler.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(_gameCommandHandler);
                } catch (NoSuchMethodException e) {
                    Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                }
            }
        } catch (NoSuchMethodException e) {
            log.error("No suitable constructor found for handler: {}, action/binding: {}", handlerClass.getName(), actionOrBinding);
            throw new RuntimeException("Failed to instantiate command handler: " + handlerClass.getName(), e);
        } catch (Exception e) {
            log.error("Failed to instantiate command handler: {}, action/binding: {}", handlerClass.getName(), actionOrBinding, e);
            throw new RuntimeException("Failed to instantiate command handler: " + handlerClass.getName(), e);
        }
    }

    public void start() throws Exception {
        _gameCommandHandler.start();
        log.info("Started GrokResponseRouter");
    }

    public void stop() {
        _gameCommandHandler.stop();
        log.info("Stopped GrokResponseRouter");
    }

    public void processGrokResponse(JsonObject jsonResponse, @Nullable String userInput) {
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
                VoiceGenerator.getInstance().speak(responseText);
                log.info("Spoke initial response: {}", responseText);
            }

            switch (type) {
                case "command":
                case "system_command":
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

    private void handleQuery(String action, JsonObject params, String userInput) {
        QueryHandler handler = queryHandlers.get(action);
        if (handler == null || action == null || action.isEmpty()) {
            handler = queryHandlers.get("general_conversation");
            action = "general_conversation";
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
                VoiceGenerator.getInstance().speak(responseTextToUse);
                log.info("Spoke final query response (action: {}): {}", action, responseTextToUse);
            } else if (requiresFollowUp) {
                JsonArray messages = new JsonArray();
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", AIContextFactory.getInstance().generateQueryPrompt());
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
                JsonObject followUpResponse = GrokQueryEndPoint.getInstance().sendToGrok(messages);

                if (followUpResponse == null) {
                    log.warn("Follow-up response is null for action: {}", action);
                    handleChat("Error accessing data banks.");
                    return;
                }

                String finalResponseText = getAsStringOrEmpty(followUpResponse, "response_text");
                if (!finalResponseText.isEmpty()) {
                    VoiceGenerator.getInstance().speak(finalResponseText);
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
        CommandHandler handler = commandHandlers.get(action);
        if (handler != null) {
            handler.handle(params, responseText);
            log.debug("Handled command action: {}", action);
        } else {
            _gameCommandHandler.handleGrokResponse(jsonResponse);
            log.debug("Delegated unhandled command to GameCommandHandler: {}", action);
        }
    }

    private void handleChat(String responseText) {
        if (!responseText.isEmpty()) {
            VoiceGenerator.getInstance().speak(responseText);
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
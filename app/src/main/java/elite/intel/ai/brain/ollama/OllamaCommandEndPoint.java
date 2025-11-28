package elite.intel.ai.brain.ollama;

import com.google.common.eventbus.Subscribe;
import com.google.gson.*;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.brain.commons.CommandEndPoint;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.StringUtls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static elite.intel.util.json.JsonUtils.getAsStringOrEmpty;

public class OllamaCommandEndPoint extends CommandEndPoint implements AiCommandInterface {

    private static final Logger log = LogManager.getLogger(OllamaCommandEndPoint.class);
    private static final OllamaCommandEndPoint INSTANCE = new OllamaCommandEndPoint();
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SystemSession systemSession = SystemSession.getInstance();

    private OllamaCommandEndPoint() {
        EventBusManager.register(this);
    }

    public static OllamaCommandEndPoint getInstance() { return INSTANCE; }

    @Override public void start() {
        if (running.compareAndSet(false, true)) {
            this.executor = java.util.concurrent.Executors.newSingleThreadExecutor(
                    r -> { Thread t = new Thread(r, "OllamaCommand-Worker"); t.setDaemon(true); return t; });
            log.info("OllamaCommandEndPoint started");
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.greeting(PlayerSession.getInstance().getPlayerName())));
        }
    }

    @Override public void stop() {
        if (running.compareAndSet(true, false)) {
            EventBusManager.unregister(this);
            if (executor != null) { executor.shutdownNow(); executor = null; }
            log.info("OllamaCommandEndPoint stopped");
        }
    }

    @Subscribe @Override public void onUserInput(UserInputEvent event) {
        if (!running.get()) return;
        if (executor == null) {
            processVoiceCommand(event.getUserInput(), event.getConfidence());
            return;
        }
        executor.submit(() -> processVoiceCommand(event.getUserInput(), event.getConfidence()));
    }

    private void processVoiceCommand(String userInput, float confidence) {
        userInput = escapeJson(userInput);
        if (userInput == null || userInput.isEmpty()) {
            getRouter().processAiResponse(createError("Sorry, I couldn't process that."), userInput);
            systemSession.clearChatHistory();
            return;
        }

        log.info("Ollama voice input: {} (conf: {})", userInput, confidence);

        JsonArray messages = buildMessageHistory();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", AIConstants.ROLE_USER);
        userMsg.addProperty("content", buildVoiceRequest(userInput));
        messages.add(userMsg);

        JsonObject response = OllamaChatEndPoint.getInstance().sendToAi(messages);
        if (response == null) {
            getRouter().processAiResponse(createError("Sorry, I couldn't reach Ollama."), userInput);
            systemSession.clearChatHistory();
            return;
        }

        getRouter().processAiResponse(response, userInput);

        // history handling – identical to your Grok version
        String type = getAsStringOrEmpty(response, "type").toLowerCase();
        if ("chat".equals(type)) {
            boolean expect = response.has(AIConstants.PROPERTY_EXPECT_FOLLOWUP) &&
                    response.get(AIConstants.PROPERTY_EXPECT_FOLLOWUP).getAsBoolean();
            JsonObject assistant = new JsonObject();
            assistant.addProperty("role", AIConstants.ROLE_ASSISTANT);
            assistant.addProperty("content", getAsStringOrEmpty(response, AIConstants.PROPERTY_RESPONSE_TEXT));
            if (expect) {
                systemSession.appendToChatHistory(userMsg, assistant);
            } else {
                systemSession.clearChatHistory();
            }
        }
    }

    @Subscribe @Override public void onSensorDataEvent(SensorDataEvent event) {
        if (!running.get()) return;

        JsonArray messages = buildMessageHistory();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", AIConstants.ROLE_USER);
        userMsg.addProperty("content", event.toJson());
        messages.add(userMsg);

        executor.submit(() -> {
            JsonObject resp = OllamaChatEndPoint.getInstance().sendToAi(messages);
            if (resp != null) getRouter().processAiResponse(resp, null);
        });
    }

    private JsonArray buildMessageHistory() {
        JsonArray messages = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", AIConstants.ROLE_SYSTEM);
        system.addProperty("content", getContextFactory().generateSystemPrompt());
        messages.add(system);

        JsonArray history = systemSession.getChatHistory();
        for (JsonElement e : history) messages.add(e);
        return messages;
    }

    private JsonObject createError(String text) {
        JsonObject err = new JsonObject();
        err.addProperty("type", AIConstants.TYPE_CHAT);
        err.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, text);
        err.add("params", new JsonObject());
        err.addProperty(AIConstants.PROPERTY_EXPECT_FOLLOWUP, true);
        return err;
    }
}
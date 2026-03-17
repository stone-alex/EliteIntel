package elite.intel.ai.brain.anthropic;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.brain.commons.CommandEndPoint;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.ChatHistory;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.StringUtls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static elite.intel.util.json.JsonUtils.getAsStringOrEmpty;
import static org.apache.logging.log4j.util.Strings.trimToNull;

/**
 * Event-driven user input processor for the Anthropic (Claude) backend.
 * <p>
 * Intentionally mirrors OllamaUserInputProcessor structure so that the two
 * implementations stay easy to diff and maintain together. The only real
 * differences are:
 * - Uses AnthropicCommandEndPoint / AnthropicClient instead of Ollama equivalents
 * - Thread name is "AnthropicCommand-Worker"
 */
public class AnthropicCommandEndPoint extends CommandEndPoint implements AiCommandInterface {

    private static final Logger log = LogManager.getLogger(AnthropicCommandEndPoint.class);
    private static final AnthropicCommandEndPoint INSTANCE = new AnthropicCommandEndPoint();

    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SystemSession systemSession = SystemSession.getInstance();

    private AnthropicCommandEndPoint() {
        // singleton
    }

    public static AnthropicCommandEndPoint getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void start() {
        EventBusManager.register(this);
        if (running.compareAndSet(false, true)) {
            this.executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "AnthropicCommand-Worker");
                t.setDaemon(true);
                return t;
            });
            log.info("AnthropicUserInputProcessor started");
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.greeting(PlayerSession.getInstance().getPlayerName())));
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            EventBusManager.unregister(this);
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
            log.info("AnthropicUserInputProcessor stopped");
        }
    }

    // -----------------------------------------------------------------------
    // Event subscribers
    // -----------------------------------------------------------------------

    @Subscribe
    @Override
    public void onUserInput(UserInputEvent event) {
        if (!running.get()) return;
        if (executor == null) {
            processVoiceCommand(event.getUserInput(), event.getConfidence());
            return;
        }
        executor.submit(() -> processVoiceCommand(event.getUserInput(), event.getConfidence()));
    }

    @Subscribe
    @Override
    public void onSensorDataEvent(SensorDataEvent event) {
        if (!running.get()) return;
        if (trimToNull(event.getSensorData()) == null) return;

        EventBusManager.publish(new AppLogEvent("Processing Sensor event"));

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", AIConstants.ROLE_SYSTEM);
        systemMessage.addProperty("content", getContextFactory().generateSensorPrompt());
        messages.add(systemMessage);

        JsonObject instructions = new JsonObject();
        instructions.addProperty("role", AIConstants.ROLE_SYSTEM);
        instructions.addProperty("content", "EVENT SPECIFIC INSTRUCTIONS: " + event.getInstructions());
        messages.add(instructions);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", AIConstants.ROLE_USER);
        userMsg.addProperty("content", event.getSensorData());
        messages.add(userMsg);

        // Low temperature for deterministic sensor-driven responses
        executor.submit(() -> {
            JsonObject response = AnthropicUserEndPoint.getInstance().processAiPrompt(messages, 0.1f);
            if (response != null) getRouter().processAiResponse(response, null);
        });
    }

    // -----------------------------------------------------------------------
    // Core processing
    // -----------------------------------------------------------------------

    private void processVoiceCommand(String userInput, float confidence) {
        if (userInput == null || userInput.isEmpty()) {
            getRouter().processAiResponse(createError("Sorry, I couldn't process that."), userInput);
            return;
        }

        log.info("Anthropic voice input: {} (conf: {})", userInput, confidence);

        JsonArray request = new JsonArray();

        // System prompt comes first – AnthropicCommandEndPoint will lift this
        // out to the top-level "system" field before sending to the API.
        JsonObject system = new JsonObject();
        system.addProperty("role", AIConstants.ROLE_SYSTEM);
        system.addProperty("content", getContextFactory().generateVoiceInputSystemPrompt());
        request.add(system);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", AIConstants.ROLE_USER);
        userMsg.addProperty("content", userInput);
        request.add(userMsg);

        // Low temperature for command classification accuracy
        JsonObject response = AnthropicUserEndPoint.getInstance().processAiPrompt(request, 0.1f);
        if (response == null) {
            getRouter().processAiResponse(createError("Sorry, I couldn't reach Claude."), userInput);
            return;
        }

        getRouter().processAiResponse(response, userInput);

        // Persist chat history on chat-type responses, identical to Ollama/Grok behaviour
        String type = getAsStringOrEmpty(response, "type").toLowerCase();
        if ("chat".equals(type)) {
            boolean expectFollowup = response.has(AIConstants.PROPERTY_EXPECT_FOLLOWUP) &&
                                     response.get(AIConstants.PROPERTY_EXPECT_FOLLOWUP).getAsBoolean();
            if (expectFollowup) {
                systemSession.setChatHistory(new ChatHistory(userInput, response.getAsString()));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private JsonObject createError(String text) {
        JsonObject err = new JsonObject();
        err.addProperty("type", AIConstants.TYPE_CHAT);
        err.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, text);
        err.add("params", new JsonObject());
        err.addProperty(AIConstants.PROPERTY_EXPECT_FOLLOWUP, true);
        return err;
    }
}
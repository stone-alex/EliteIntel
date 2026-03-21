package elite.intel.ai.brain.ollama;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.brain.AiCommandsAndQueries;
import elite.intel.ai.brain.commons.CommandEndPoint;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.StringUtls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.logging.log4j.util.Strings.trimToNull;

public class OllamaUserInputProcessor extends CommandEndPoint implements AiCommandInterface {

    private static final Logger log = LogManager.getLogger(OllamaUserInputProcessor.class);
    private static final OllamaUserInputProcessor INSTANCE = new OllamaUserInputProcessor();

    // Actions the LLM uses as a catch-all - only permitted when input contains at least one required keyword
    private static final Map<String, List<String>> GUARDED_ACTIONS = Map.of(
            "query_key_bindings_analysis", List.of("key", "bind", "binding", "keybind", "unbound"),
            "query_app_capabilities", List.of("capabilit", "what can you", "your function", "what do you do", "commands do you")
    );
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private OllamaUserInputProcessor() {
        //
    }

    public static OllamaUserInputProcessor getInstance() { return INSTANCE; }

    @Override public void start() {
        EventBusManager.register(this);
        if (running.compareAndSet(false, true)) {
            this.executor = java.util.concurrent.Executors.newSingleThreadExecutor(
                    r -> { Thread t = new Thread(r, "OllamaCommand-Worker"); t.setDaemon(true); return t; });
            log.info("OllamaCommandEndPoint started");
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.greeting(PlayerSession.getInstance().getPlayerName()))
            );
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
        if (userInput == null || userInput.isEmpty()) {
            getRouter().processAiResponse(createError("Sorry, I couldn't process that."), userInput);
            return;
        }

        log.info("Ollama voice input: {} (conf: {})", userInput, confidence);

        if (CONNECTION_CHECK_COMMAND.equalsIgnoreCase(userInput)) {
            JsonObject direct = new JsonObject();
            direct.addProperty("action", CONNECTION_CHECK_COMMAND);
            direct.add("params", new JsonObject());
            getRouter().processAiResponse(direct, userInput);
            return;
        }

        String directAction = AiCommandsAndQueries.getInstance().matchCommand(userInput);
        if (directAction != null) {
            log.info("Direct command match (bypassing LLM): {} → {}", userInput, directAction);
            JsonObject direct = new JsonObject();
            direct.addProperty("action", directAction);
            direct.add("params", new JsonObject());
            getRouter().processAiResponse(direct, userInput);
            return;
        }

        JsonArray request = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", AIConstants.ROLE_SYSTEM);
        system.addProperty("content", getContextFactory().generateUserInputSystemPrompt());
        request.add(system);
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", AIConstants.ROLE_USER);
        userMsg.addProperty("content", userInput);
        request.add(userMsg);

        JsonObject response = OllamaCommandEndPoint.getInstance().processAiPrompt(request, 0.01f);
        if (response == null) {
            getRouter().processAiResponse(createError("LLM did not answer."), userInput);
            return;
        }

        getRouter().processAiResponse(guardResponse(response, userInput), userInput);

    }

    @Subscribe @Override public void onSensorDataEvent(SensorDataEvent event) {
        if (!running.get()) return;
        if (trimToNull(event.getSensorData()) == null) return;
        EventBusManager.publish(new AppLogEvent("Processing Sensor event"));
        JsonObject response = OllamaAnalysisEndpoint.getInstance().processSensor(event);
        getRouter().processAiResponse(response, "");
    }

    /**
     * Rejects guarded actions when the input doesn't contain any of their required keywords.
     */
    private JsonObject guardResponse(JsonObject response, String input) {
        String action = response.has("action") ? response.get("action").getAsString() : "";
        List<String> required = GUARDED_ACTIONS.get(action);
        if (required == null) return response;
        String lower = input.toLowerCase();
        boolean permitted = required.stream().anyMatch(lower::contains);
        if (!permitted) {
            log.warn("Guarded action {} rejected for input: {}", action, input);
            JsonObject fallback = new JsonObject();
            fallback.addProperty("action", "command_not_found");
            fallback.add("params", new JsonObject());
            return fallback;
        }
        return response;
    }

    private JsonObject createError(String text) {
        JsonObject err = new JsonObject();
        err.addProperty(AIConstants.PROPERTY_TEXT_TO_SPEECH_RESPONSE, text);
        return err;
    }
}
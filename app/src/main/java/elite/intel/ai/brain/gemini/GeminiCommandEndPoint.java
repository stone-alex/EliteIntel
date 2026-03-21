package elite.intel.ai.brain.gemini;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiCommandInterface;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static elite.intel.util.json.JsonUtils.getAsStringOrEmpty;
import static org.apache.logging.log4j.util.Strings.trimToNull;

public class GeminiCommandEndPoint extends CommandEndPoint implements AiCommandInterface {
    private static final Logger log = LogManager.getLogger(GeminiCommandEndPoint.class);
    private static GeminiCommandEndPoint instance;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executor;

    private GeminiCommandEndPoint() {
    }

    public static GeminiCommandEndPoint getInstance() {
        if (instance == null) {
            instance = new GeminiCommandEndPoint();
        }
        return instance;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            this.executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "GeminiCommandEndPoint-Worker");
                t.setDaemon(true);
                return t;
            });
            EventBusManager.register(this);
            log.info("GeminiCommandEndPoint started");
            EventBusManager.publish(new AiVoxResponseEvent(StringUtls.greeting(PlayerSession.getInstance().getPlayerName())));
        } else {
            log.debug("GeminiCommandEndPoint already started");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            EventBusManager.unregister(this);
            if (executor != null) {
                GeminiClient.getInstance().cancelCurrentRequest();
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException ie) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                } finally {
                    executor = null;
                }
            }
            log.info("GeminiCommandEndPoint stopped");
        } else {
            log.debug("GeminiCommandEndPoint already stopped");
        }
    }

    @Subscribe
    @Override
    public void onUserInput(UserInputEvent event) {
        if (!running.get()) {
            log.debug("Ignoring onUserInput: endpoint not running");
            return;
        }
        if (executor == null) {
            log.warn("Executor is null; running onUserInput on caller thread");
            processVoiceCommand(event.getUserInput(), event.getConfidence());
            return;
        }
        executor.submit(() -> {
            try {
                processVoiceCommand(event.getUserInput(), event.getConfidence());
            } catch (Exception e) {
                log.error("Error processing user input", e);
            }
        });
    }

    private void processVoiceCommand(String userInput, float confidence) {
        if (userInput == null || userInput.isEmpty()) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty(AIConstants.PROPERTY_TEXT_TO_SPEECH_RESPONSE, "Sorry, I couldn't process that.");
            getRouter().processAiResponse(errorResponse, userInput);
            return;
        }

        log.info("Sanitized voice userInput:\n{} (confidence: {})", userInput, confidence);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", AIConstants.ROLE_SYSTEM);
        systemMessage.addProperty("content", getContextFactory().generateUserInputSystemPrompt());
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", AIConstants.ROLE_USER);
        userMessage.addProperty("content", userInput);
        messages.add(userMessage);

        JsonObject apiResponse = getChatInterface().processAiPrompt(messages, 0.01f);
        if (apiResponse == null) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty(AIConstants.PROPERTY_TEXT_TO_SPEECH_RESPONSE, "Sorry, I couldn't process that.");
            getRouter().processAiResponse(errorResponse, userInput);
            return;
        }

        log.info("Processing Action: {}", getAsStringOrEmpty(apiResponse, AIConstants.TYPE_ACTION));
        getRouter().processAiResponse(apiResponse, userInput);
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
        instructions.addProperty("content", event.getInstructions());
        messages.add(instructions);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", AIConstants.ROLE_USER);
        userMessage.addProperty("content", event.getSensorData());
        messages.add(userMessage);

        executor.submit(() -> {
            try {
                JsonObject apiResponse = getChatInterface().processAiPrompt(messages, 0.01f);
                if (apiResponse == null) {
                    EventBusManager.publish(new AiVoxResponseEvent("Failure processing system request. Check programming"));
                    return;
                }
                getRouter().processAiResponse(apiResponse, null);
            } catch (JsonSyntaxException e) {
                log.error("JSON parsing failed for sensor event", e);
            }
        });
    }
}

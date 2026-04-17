package elite.intel.ai.brain.xai;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.brain.commons.CommandEndPoint;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static elite.intel.util.json.JsonUtils.getAsStringOrEmpty;
import static org.apache.logging.log4j.util.Strings.trimToNull;

public class GrokCommandEndPoint extends CommandEndPoint implements AiCommandInterface {
    private static final Logger log = LogManager.getLogger(GrokCommandEndPoint.class);
    private static GrokCommandEndPoint instance;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executor;

    private GrokCommandEndPoint() {
    }

    public static GrokCommandEndPoint getInstance() {
        if (instance == null) {
            instance = new GrokCommandEndPoint();
        }
        return instance;
    }

    @Override public void start() {
        if (running.compareAndSet(false, true)) {
            this.executor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "GrokCommandEndPoint-Worker");
                t.setDaemon(true);
                return t;
            });
            EventBusManager.register(this);
            log.info("GrokCommandEndPoint started");
        } else {
            log.debug("GrokCommandEndPoint already started");
        }
    }

    @Override public void stop() {
        if (running.compareAndSet(true, false)) {
            EventBusManager.unregister(this);
            if (executor != null) {
                GrokClient.getInstance().cancelCurrentRequest();
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException ie) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                } finally {
                    executor = null;
                }
            }
            log.info("GrokCommandEndPoint stopped");
        } else {
            log.debug("GrokCommandEndPoint already stopped");
        }
    }


    @Subscribe @Override public void onUserInput(UserInputEvent event) {
        if (!running.get()) {
            log.debug("Ignoring onUserInput: endpoint not running");
            return;
        }
        if (executor == null) {
            log.warn("Executor is null; running onUserInput on caller thread");
            processVoiceCommand(event.getUserInput());
            return;
        }
        executor.submit(() -> {
            try {
                processVoiceCommand(event.getUserInput());
            } catch (Exception e) {
                log.error("Error processing user input", e);
            }
        });
    }

    private void processVoiceCommand(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            getRouter().processAiResponse(createError("Sorry, I couldn't process that."), userInput);
            return;
        }

        log.info("Sanitized voice userInput:\n{}", userInput);

        JsonArray messages = buildVoiceCommandMessages(userInput);

        JsonObject apiResponse = GrokChatEndPoint.getInstance().processAiPrompt(messages, 0.01f);
        if (apiResponse == null) {
            getRouter().processAiResponse(createError("Sorry, I couldn't process that."), userInput);
            return;
        }

        log.info("Processing Action: {}", getAsStringOrEmpty(apiResponse, AIConstants.TYPE_ACTION));
        getRouter().processAiResponse(apiResponse, userInput);
    }

    @Subscribe @Override public void onSensorDataEvent(SensorDataEvent event) {
        if (!running.get()) return;
        if (trimToNull(event.getSensorData()) == null) return;

        EventBusManager.publish(new AppLogEvent("Processing Sensor event"));
        JsonArray messages = buildSensorMessages(event);

        executor.submit(() -> {
            try {
                JsonObject apiResponse = callXaiApi(messages);
                if (apiResponse == null) {
                    EventBusManager.publish(new AiVoxResponseEvent("Failure processing system request. Check programming"));
                    return;
                }
                getRouter().processAiResponse(apiResponse, null);
            } catch (JsonSyntaxException e) {
                log.error("JSON parsing failed for sensor event", e);
                throw e;
            }
        });
    }

    private JsonObject callXaiApi(JsonArray messages) {
        try {
            GrokClient client = GrokClient.getInstance();
            JsonObject prompt = client.createPrompt(GrokClient.MODEL_GROK_REASONING, 1.00f);
            prompt.add("messages", messages);
            String jsonString = GsonFactory.getGson().toJson(prompt);

            HttpURLConnection conn = client.getHttpURLConnection();
            JsonObject response = processAiPrompt(jsonString, client);

            JsonArray choices = response.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("No choices in API response:\n{}", response);
                return client.createErrorResponse("No choices in API response");
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                log.error("No message in API response choices:\n{}", response);
                return client.createErrorResponse("No message in API response");
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                log.error("No content in API response message:\n{}", response);
                return client.createErrorResponse("No content in API response");
            }

            log.debug("Raw API content (before cleanup):\n{}", content);

            // Robust JSON extraction - handles ```json
            String jsonContent = extractJsonFromContent(content);
            if (jsonContent == null) {
                log.error("Could not extract valid JSON from content:\n{}", content);
                return client.createErrorResponse("Could not extract JSON from model response");
            }

            log.debug("Extracted JSON:\n{}", jsonContent);

            return JsonParser.parseString(jsonContent).getAsJsonObject();

        } catch (IOException e) {
            log.error("X AI API call failed: {}", e.getMessage(), e);
            String aiServerError = serverErrorMessage(e);
            return GrokClient.getInstance().createErrorResponse("API call failed: " + aiServerError);
        } catch (JsonSyntaxException e) {
            log.error("JSON parse failed after extraction", e);
            return GrokClient.getInstance().createErrorResponse("Invalid JSON structure from model");
        }
    }

    private String serverErrorMessage(Exception e) {
        String aiServerError = e.getMessage();
        if (e.getMessage().contains("500") || e.getMessage().contains("402") || e.getMessage().contains("401")) {
            aiServerError = "AI Internal server crash.";
        }
        if (e.getMessage().contains("404")) {
            aiServerError = "AI API URL is invalid";
        }
        if (e.getMessage().contains("403")) {
            aiServerError = "AI API key is invalid, or not authorized for this endpoint.";
        }
        return aiServerError;
    }
}
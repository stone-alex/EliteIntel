package elite.intel.ai.brain.xai;

import com.google.common.eventbus.Subscribe;
import com.google.gson.*;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.brain.commons.CommandEndPoint;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.SystemSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrokCommandEndPoint extends CommandEndPoint implements AiCommandInterface {
    private static final Logger log = LogManager.getLogger(GrokCommandEndPoint.class);
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private static GrokCommandEndPoint instance;
    private SystemSession systemSession;

    public static GrokCommandEndPoint getInstance() {
        if (instance == null) {
            instance = new GrokCommandEndPoint();
        }
        return instance;
    }

    private GrokCommandEndPoint() {
        systemSession = SystemSession.getInstance();
        EventBusManager.register(this);
    }


    @Override public void start() {
        if (running.compareAndSet(false, true)) {
            this.executor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "GrokCommandEndPoint-Worker");
                t.setDaemon(true);
                return t;
            });
            elite.intel.gameapi.EventBusManager.register(this);
            log.info("GrokCommandEndPoint started");
            EventBusManager.publish(new AiVoxResponseEvent("X Ai is Online."));
        } else {
            log.debug("GrokCommandEndPoint already started");
        }
    }

    @Override public void stop() {
        if (running.compareAndSet(true, false)) {
            elite.intel.gameapi.EventBusManager.unregister(this);
            if (executor != null) {
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

        userInput = escapeJson(userInput);
        if (userInput == null || userInput.isEmpty()) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("type", AIConstants.TYPE_CHAT);
            errorResponse.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, "Sorry, I couldn't process that.");
            errorResponse.addProperty(AIConstants.TYPE_ACTION, (String) null);
            errorResponse.add("params", new JsonObject());
            errorResponse.addProperty(AIConstants.PROPERTY_EXPECT_FOLLOWUP, true);
            getRouter().processAiResponse(errorResponse, userInput);
            systemSession.clearChatHistory();
            return;
        }

        log.info("Sanitized voice userInput:\n{} (confidence: {})", userInput, confidence);

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", AIConstants.ROLE_SYSTEM);
        String systemPrompt = getContextFactory().generateSystemPrompt();
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonArray history = SystemSession.getInstance().getChatHistory();
        for (int i = 0; i < history.size(); i++) {
            messages.add(history.get(i));
        }

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", AIConstants.ROLE_USER);
        String userContent = buildVoiceRequest(userInput);
        userMessage.addProperty("content", userContent);
        messages.add(userMessage);

        // Send via GrokChatEndPoint
        JsonObject apiResponse = getChatInterface().sendToAi(messages);
        if (apiResponse == null) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("type", AIConstants.TYPE_CHAT);
            errorResponse.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, "Sorry, I couldn't process that.");
            errorResponse.addProperty(AIConstants.TYPE_ACTION, (String) null);
            errorResponse.add("params", new JsonObject());
            errorResponse.addProperty(AIConstants.PROPERTY_EXPECT_FOLLOWUP, true);
            getRouter().processAiResponse(errorResponse, userInput);
            systemSession.clearChatHistory();
            return;
        }

        getRouter().processAiResponse(apiResponse, userInput);

        String type = JsonUtils.getAsStringOrEmpty(apiResponse, "type").toLowerCase();
        String responseText = JsonUtils.getAsStringOrEmpty(apiResponse, AIConstants.PROPERTY_RESPONSE_TEXT);
        if ("chat".equals(type)) {
            boolean expectFollowup = apiResponse.has(AIConstants.PROPERTY_EXPECT_FOLLOWUP) && apiResponse.get(AIConstants.PROPERTY_EXPECT_FOLLOWUP).getAsBoolean();
            JsonObject assistantMessage = new JsonObject();
            assistantMessage.addProperty("role", AIConstants.ROLE_ASSISTANT);
            assistantMessage.addProperty("content", responseText);

            if (expectFollowup) {
                systemSession.appendToChatHistory(userMessage, assistantMessage);
            } else {
                systemSession.clearChatHistory();
            }
        }
    }

    @Subscribe @Override public void onSensorDataEvent(SensorDataEvent event) {
        if (!running.get()) {
            log.debug("Ignoring onSensorDataEvent: endpoint not running");
            return;
        }


        String input = event.getSensorData();


        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", AIConstants.ROLE_SYSTEM);
        String systemPrompt = getContextFactory().generateSystemPrompt();
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", AIConstants.ROLE_USER);
        userMessage.addProperty("content", buildSystemRequest(input));
        messages.add(userMessage);

        GrokClient client = GrokClient.getInstance();
        JsonObject requestBody = client.createRequestBodyHeader(GrokClient.MODEL_GROK_4_FAST_NON_REASONING, 0.01f);
        requestBody.add("messages", messages);

        Gson gson = GsonFactory.getGson();
        String jsonString = gson.toJson(requestBody);
        log.debug("JSON prepared for callXaiApi:\n{}", jsonString);
        executor.submit(() -> {
            try {
                JsonObject apiResponse = callXaiApi(jsonString);
                if (apiResponse == null) {
                    EventBusManager.publish(new AiVoxResponseEvent("Failure processing system request. Check programming"));
                    return;
                }
                getRouter().processAiResponse(apiResponse, null);
            } catch (JsonSyntaxException e) {
                log.error("JSON parsing failed for input:\n{}", jsonString, e);
                throw e;
            }
        });
    }

    private JsonObject callXaiApi(String jsonString) {
        try {
            GrokClient client = GrokClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();

            log.debug("xAI API call:\n{}", jsonString);
            JsonObject requestBody = GsonFactory.getGson().fromJson(jsonString, JsonObject.class);
            JsonArray messages = requestBody.getAsJsonArray("messages");
            systemSession.setChatHistory(messages);


            Response response = callApi(conn, jsonString, client);
            StracturedResponse stracturedResponse = checkResponse(response);
            if (!stracturedResponse.isSuccessful()) return null;

            log.debug("API response content:\n{}", stracturedResponse.content());

            String jsonContent;
            int jsonStart = stracturedResponse.content().indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = stracturedResponse.content().substring(jsonStart + 2); // Skip \n\n
            } else {
                jsonStart = stracturedResponse.content().indexOf("{");
                if (jsonStart == -1) {
                    log.error("No JSON object found in content:\n{}", stracturedResponse.content());
                    return null;
                }
                jsonContent = stracturedResponse.content().substring(jsonStart);
                try {
                    JsonParser.parseString(jsonContent);
                } catch (JsonSyntaxException e) {
                    log.error("Invalid JSON object in content:\n{}", jsonContent, e);
                    return null;
                }
            }

            log.info("Extracted JSON content:\n\n{}\n\n", jsonContent);

            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content:\n{}", jsonContent, e);
                throw e;
            }
        } catch (Exception e) {
            String aiServerError = serverErrorMessage(e);
            log.error("AI API call failed: {}", e.getMessage(), e);
            log.error("Input data:\n{}", jsonString);
            return GrokClient.getInstance().createErrorResponse("API call failed: " + aiServerError);
        } finally {
            systemSession.clearChatHistory();
        }
    }

    private String serverErrorMessage(Exception e) {
        String aiServerError = e.getMessage();
        if (e.getMessage().contains("500") || e.getMessage().contains("402") || e.getMessage().contains("401")) {
            aiServerError = "AI Internal server crash.";
        }
        if(e.getMessage().contains("404")){
            aiServerError = "AI API URL is invalid";
        }
        if(e.getMessage().contains("403")){
            aiServerError = "AI API key is invalid, or not authorized for this endpoint.";
        }
        return aiServerError;
    }
}
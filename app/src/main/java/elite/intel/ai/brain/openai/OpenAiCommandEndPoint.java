package elite.intel.ai.brain.openai;

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
import elite.intel.session.SystemSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenAiCommandEndPoint extends CommandEndPoint implements AiCommandInterface {
    private static final Logger log = LogManager.getLogger(OpenAiCommandEndPoint.class);
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SystemSession systemSession;

    private static OpenAiCommandEndPoint instance;

    public static OpenAiCommandEndPoint getInstance() {
        if (instance == null) {
            instance = new OpenAiCommandEndPoint();
        }
        return instance;
    }

    private OpenAiCommandEndPoint() {
        systemSession = SystemSession.getInstance();
        EventBusManager.register(this);
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            this.executor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "OpenAiCommandEndPoint-Worker");
                t.setDaemon(true);
                return t;
            });
            EventBusManager.register(this);
            log.info("OpenAiCommandEndPoint started");
            EventBusManager.publish(new AiVoxResponseEvent("Open AI is Online."));
        } else {
            log.debug("OpenAiCommandEndPoint already started");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            EventBusManager.unregister(this);
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
            log.info("OpenAiCommandEndPoint stopped");
        } else {
            log.debug("OpenAiCommandEndPoint already stopped");
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
        // Sanitize input
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

        // Log sanitized input
        log.info("Sanitized voice userInput:\n{} (confidence: {})", userInput, confidence);

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", AIConstants.ROLE_SYSTEM);
        String systemPrompt = getContextFactory().generateSystemPrompt();
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        // Append existing chat history if any
        JsonArray history = systemSession.getChatHistory();
        for (int i = 0; i < history.size(); i++) {
            messages.add(history.get(i));
        }

        // Add current user message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", AIConstants.ROLE_USER);
        String userContent = buildVoiceRequest(userInput);
        userMessage.addProperty("content", userContent);
        messages.add(userMessage);

        // Send to Open AI
        JsonObject apiResponse = callOpenAiApi(messages);
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

        // Route the response
        getRouter().processAiResponse(apiResponse, userInput);

        // Handle history updates for chat continuations
        String type = JsonUtils.getAsStringOrEmpty(apiResponse, "type").toLowerCase();
        String responseText = JsonUtils.getAsStringOrEmpty(apiResponse, AIConstants.PROPERTY_RESPONSE_TEXT);
        if ("chat".equals(type)) {
            boolean expectFollowup = apiResponse.has(AIConstants.PROPERTY_EXPECT_FOLLOWUP) && apiResponse.get(AIConstants.PROPERTY_EXPECT_FOLLOWUP).getAsBoolean();
            JsonObject assistantMessage = new JsonObject();
            assistantMessage.addProperty("role", AIConstants.ROLE_ASSISTANT);
            assistantMessage.addProperty("content", responseText);

            if (expectFollowup) {
                systemSession.appendToChatHistory( userMessage, assistantMessage);
            } else {
                systemSession.clearChatHistory();
            }
        }
    }

    @Subscribe
    @Override
    public void onSensorDataEvent(SensorDataEvent event) {
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

        // Create API request body
        OpenAiClient client = OpenAiClient.getInstance();
        JsonObject requestBody = client.createRequestBodyHeader(OpenAiClient.MODEL_GPT_4_1_MINI, 0.10f);
        requestBody.add("messages", messages);

        // Serialize to JSON string
        String jsonString = GsonFactory.getGson().toJson(requestBody);
        log.debug("JSON prepared for callOpenAiApi:\n{}", jsonString);
        executor.submit(() -> {
            try {
                JsonObject apiResponse = callOpenAiApi(messages);
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

    private JsonObject callOpenAiApi(JsonArray messages) {
        try {
            OpenAiClient client = OpenAiClient.getInstance();
            JsonObject requestBody = client.createRequestBodyHeader(OpenAiClient.MODEL_GPT_4_1_MINI, 0.10f);
            requestBody.add("messages", messages);
            String jsonString = GsonFactory.getGson().toJson(requestBody);
            log.debug("Open AI API call:\n{}", jsonString);

            // Store messages for history
            systemSession.setChatHistory(messages);

            HttpURLConnection conn = client.getHttpURLConnection();
            Response response = callApi(conn, jsonString, client);

            // Extract content
            JsonArray choices = response.responseData().getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("No choices in API response:\n{}", response.responseMessage());
                return client.createErrorResponse("No choices in API response");
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                log.error("No message in API response choices:\n{}", response.responseMessage());
                return client.createErrorResponse("No message in API response");
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                log.error("No content in API response message:\n{}", response.responseMessage());
                return client.createErrorResponse("No content in API response");
            }

            // Log content before parsing
            log.debug("API response content:\n{}", content);

            // Parse JSON content
            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                jsonStart = content.indexOf("{");
                if (jsonStart == -1) {
                    log.error("No JSON object found in content:\n{}", content);
                    return client.createErrorResponse("No JSON object in content");
                }
                jsonContent = content.substring(jsonStart);
                try {
                    JsonParser.parseString(jsonContent);
                } catch (JsonSyntaxException e) {
                    log.error("Invalid JSON object in content:\n{}", jsonContent, e);
                    return client.createErrorResponse("Invalid JSON in content");
                }
            }

            // Log extracted JSON
            log.info("Extracted JSON content:\n\n{}\n\n", GsonFactory.getGson().toJson(jsonContent));

            // Parse JSON content
            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content:\n\n{}\n\n", GsonFactory.getGson().toJson(jsonContent), e);
                return client.createErrorResponse("Failed to parse API content");
            }
        } catch (IOException e) {
            log.error("Open AI API call failed: {}", e.getMessage(), e);
            String aiServerError = serverErrorMessage(e);
            return OpenAiClient.getInstance().createErrorResponse("API call failed: " + aiServerError);
        } finally {
            systemSession.clearChatHistory();
        }
    }

    private static String serverErrorMessage(IOException e) {
        String aiServerError = "AI Internal server error.";
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
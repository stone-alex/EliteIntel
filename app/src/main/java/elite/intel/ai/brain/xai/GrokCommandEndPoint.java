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
import elite.intel.util.SleepNoThrow;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The GrokCommandEndPoint class implements the AiCommandInterface to handle
 * AI conversational interactions and sensor data events. It processes user-inputted
 * commands, sensor data, and communicates with AI services to generate responses.
 * This class interacts with various components, including a router, chat interface,
 * and context factory.
 * <p>
 * It provides mechanisms to:
 * - Start and stop the endpoint.
 * - Process user voice commands with error-handling, confidence-checking, and sanitization.
 * - Process sensor data events with system context and generate AI responses.
 * - Manage chat history for continued conversations.
 * - Communicate with external AI APIs via JSON serialization and HTTP calls.
 * <p>
 * The class utilizes the thread-safe ThreadLocal storage for maintaining the context
 * of current conversations and ensures robust error handling and logging during its operation.
 * <p>
 * Events handled:
 * - UserInputEvent: Captures user voice commands and processes them.
 * - SensorDataEvent: Handles system-generated sensor data for AI interaction.
 * <p>
 * Dependencies:
 * - Router for handling AI response routing.
 * - AI Chat Interface for communicating with the AI engine.
 * - AI Context Factory for generating system prompts.
 * - Gson for JSON processing.
 * - EventBus for subscribing and publishing events.
 * - Logging framework for detailed debugging and error reporting.
 */
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
        log.info("Sanitized voice userInput: [{}] (confidence: {})", toDebugString(userInput), confidence);

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", AIConstants.ROLE_SYSTEM);
        String systemPrompt = getContextFactory().generateSystemPrompt();
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        // Append existing chat history if any
        JsonArray history = SystemSession.getInstance().getChatHistory();
        for (int i = 0; i < history.size(); i++) {
            messages.add(history.get(i));
        }

        // Add current user message
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

        // Route the response without speaking here
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

        // Create API request body
        GrokClient client = GrokClient.getInstance();
        JsonObject requestBody = client.createRequestBodyHeader(GrokClient.MODEL_GROK_4_FAST_NON_REASONING, 0.01f);
        requestBody.add("messages", messages);

        // Serialize to JSON string
        Gson gson = GsonFactory.getGson();
        String jsonString = gson.toJson(requestBody);
        log.debug("JSON prepared for callXaiApi: [{}]", toDebugString(jsonString));
        executor.submit(() -> {
            try {
                JsonObject apiResponse = callXaiApi(jsonString);
                if (apiResponse == null) {
                    EventBusManager.publish(new AiVoxResponseEvent("Failure processing system request. Check programming"));
                    return;
                }
                getRouter().processAiResponse(apiResponse, null);
            } catch (JsonSyntaxException e) {
                log.error("JSON parsing failed for input: [{}]", toDebugString(jsonString), e);
                throw e;
            }
        });
    }

    private JsonObject callXaiApi(String jsonString) {
        try {
            HttpURLConnection conn = GrokClient.getInstance().getHttpURLConnection();

            // Log the input string
            log.debug("xAI API call: [{}]", toDebugString(jsonString));
            // Store the messages array from the request body
            JsonObject requestBody = GsonFactory.getGson().fromJson(jsonString, JsonObject.class);
            JsonArray messages = requestBody.getAsJsonArray("messages");
            systemSession.setChatHistory(messages);

            try (var os = conn.getOutputStream()) {
                os.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String response;
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }

            // Strip BOM if present
            if (response.startsWith("\uFEFF")) {
                response = response.substring(1);
                log.info("Stripped BOM from response");
            }

            // Log raw response
            log.debug("xAI API response: [{}]", toDebugString(response));

            if (responseCode != 200) {
                String errorResponse = "";
                try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                    errorResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                } catch (Exception e) {
                    log.warn("Failed to read error stream: {}", e.getMessage());
                }
                log.error("xAI API error: {} - {}", responseCode, conn.getResponseMessage());
                log.info("Error response body: {}", errorResponse);
                return null;
            }

            // Parse response
            JsonObject json;
            try {
                json = JsonParser.parseString(response).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response: [{}]", toDebugString(response), e);
                throw e;
            }

            // Extract content safely
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("No choices in API response: [{}]", toDebugString(response));
                return null;
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                log.error("No message in API response choices: [{}]", toDebugString(response));
                return null;
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                log.error("No content in API response message: [{}]", toDebugString(response));
                return null;
            }

            // Log content before parsing
            log.debug("API response content: [{}]", toDebugString(content));

            // Extract JSON from content (after double newline or first valid JSON object)
            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                // Fallback: Find first { that starts a valid JSON object
                jsonStart = content.indexOf("{");
                if (jsonStart == -1) {
                    log.error("No JSON object found in content: [{}]", toDebugString(content));
                    return null;
                }
                jsonContent = content.substring(jsonStart);
                // Validate JSON
                try {
                    JsonParser.parseString(jsonContent);
                } catch (JsonSyntaxException e) {
                    log.error("Invalid JSON object in content: [{}]", toDebugString(jsonContent), e);
                    return null;
                }
            }

            // Log extracted JSON
            log.info("Extracted JSON content: [{}]", toDebugString(jsonContent));

            // Parse JSON content
            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content: [{}]", toDebugString(jsonContent), e);
                throw e;
            }
        } catch (Exception e) {
            String aiServerError = serverErrorMessage(e);
            log.error("AI API call failed: {}", e.getMessage(), e);
            log.error("Input data: [{}]", toDebugString(jsonString));
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
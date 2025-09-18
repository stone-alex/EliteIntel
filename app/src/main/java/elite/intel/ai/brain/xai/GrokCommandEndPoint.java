package elite.intel.ai.brain.xai;

import com.google.common.eventbus.Subscribe;
import com.google.gson.*;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.AIRouterInterface;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.brain.AiContextFactory;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.session.SystemSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * The GrokCommandEndPoint class implements the AiCommandInterface to handle
 * AI conversational interactions and sensor data events. It processes user-inputted
 * commands, sensor data, and communicates with AI services to generate responses.
 * This class interacts with various components including a router, chat interface,
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
public class GrokCommandEndPoint implements AiCommandInterface {
    private static final Logger log = LogManager.getLogger(GrokCommandEndPoint.class);
    private java.util.concurrent.ExecutorService executor;
    private final java.util.concurrent.atomic.AtomicBoolean running = new java.util.concurrent.atomic.AtomicBoolean(false);

    private static final ThreadLocal<JsonArray> currentHistory = new ThreadLocal<>();
    private final AIRouterInterface router;
    private final AIChatInterface chatInterface;
    private final AiContextFactory contextFactory;

    private static GrokCommandEndPoint instance;

    public static GrokCommandEndPoint getInstance() {
        if (instance == null) {
            instance = new GrokCommandEndPoint();
        }
        return instance;
    }

    private GrokCommandEndPoint() {
        this.router = ApiFactory.getInstance().getAiRouter();
        this.chatInterface = ApiFactory.getInstance().getChatEndpoint();
        this.contextFactory = ApiFactory.getInstance().getAiContextFactory();
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
            errorResponse.addProperty("type", "chat");
            errorResponse.addProperty("response_text", "Sorry, I couldn't process that.");
            errorResponse.addProperty("action", (String) null);
            errorResponse.add("params", new JsonObject());
            errorResponse.addProperty("expect_followup", true);
            router.processAiResponse(errorResponse, userInput);
            SystemSession.getInstance().clearChatHistory();
            return;
        }

        // Check STT confidence
        if (confidence < 0.5f) {
            JsonArray messages = new JsonArray();
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            String systemPrompt = contextFactory.generateSystemPrompt();
            systemMessage.addProperty("content", systemPrompt);
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            String userContent = buildVoiceRequest(userInput);
            userMessage.addProperty("content", userContent);
            messages.add(userMessage);

            // Create clarification response
            JsonObject clarification = new JsonObject();
            clarification.addProperty("type", "chat");
            clarification.addProperty("response_text", "Say again?");
            clarification.addProperty("action", (String) null);
            clarification.add("params", new JsonObject());
            clarification.addProperty("expect_followup", true);

            // Route clarification without direct TTS
            router.processAiResponse(clarification, userInput);

            // Store user message in history for follow-up
            JsonObject assistantMessage = new JsonObject();
            assistantMessage.addProperty("role", "assistant");
            assistantMessage.addProperty("content", "Say again?");
            SystemSession.getInstance().appendToChatHistory(userMessage, assistantMessage);
            return;
        }

        // Log sanitized input
        log.info("Sanitized voice userInput: [{}] (confidence: {})", toDebugString(userInput), confidence);

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        String systemPrompt = contextFactory.generateSystemPrompt();
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        // Append existing chat history if any
        JsonArray history = SystemSession.getInstance().getChatHistory();
        for (int i = 0; i < history.size(); i++) {
            messages.add(history.get(i));
        }

        // Add current user message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        String userContent = buildVoiceRequest(userInput);
        userMessage.addProperty("content", userContent);
        messages.add(userMessage);

        // Send via GrokChatEndPoint
        JsonObject apiResponse = chatInterface.sendToAi(messages);
        if (apiResponse == null) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("type", "chat");
            errorResponse.addProperty("response_text", "Sorry, I couldn't process that.");
            errorResponse.addProperty("action", (String) null);
            errorResponse.add("params", new JsonObject());
            errorResponse.addProperty("expect_followup", true);
            router.processAiResponse(errorResponse, userInput);
            SystemSession.getInstance().clearChatHistory();
            return;
        }

        // Route the response without speaking here
        router.processAiResponse(apiResponse, userInput);

        // Handle history updates for chat continuations
        String type = JsonUtils.getAsStringOrEmpty(apiResponse, "type").toLowerCase();
        String responseText = JsonUtils.getAsStringOrEmpty(apiResponse, "response_text");
        if ("chat".equals(type)) {
            boolean expectFollowup = apiResponse.has("expect_followup") && apiResponse.get("expect_followup").getAsBoolean();
            JsonObject assistantMessage = new JsonObject();
            assistantMessage.addProperty("role", "assistant");
            assistantMessage.addProperty("content", responseText);

            if (expectFollowup) {
                SystemSession.getInstance().appendToChatHistory(userMessage, assistantMessage);
            } else {
                SystemSession.getInstance().clearChatHistory();
            }
        } else {
            SystemSession.getInstance().clearChatHistory();
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
        systemMessage.addProperty("role", "system");
        String systemPrompt = contextFactory.generateSystemPrompt();
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", buildSystemRequest(input));
        messages.add(userMessage);

        // Create API request body
        GrokClient client = GrokClient.getInstance();
        JsonObject requestBody = client.createRequestBodyHeader(GrokClient.MODEL_GROK_3_FAST);
        requestBody.add("messages", messages);

        // Serialize to JSON string
        Gson gson = GsonFactory.getGson();
        String jsonString = gson.toJson(requestBody);
        log.debug("JSON prepared for callXaiApi: [{}]", toDebugString(jsonString));
        executor.submit(() -> {
            try {
                JsonObject apiResponse = callXaiApi(jsonString);
                if (apiResponse == null) {
                    EventBusManager.publish(new VoiceProcessEvent("Failure processing system request. Check programming"));
                    return;
                }
                router.processAiResponse(apiResponse, null);
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
            currentHistory.set(messages); // Store messages array

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
            log.error("AI API call fatal error: {}", e.getMessage(), e);
            log.error("Input data: [{}]", toDebugString(jsonString));
            return null;
        } finally {
            currentHistory.remove(); // Always clear
        }
    }

    public static JsonArray getCurrentHistory() {
        return currentHistory.get() != null ? currentHistory.get() : new JsonArray();
    }

    private String buildVoiceRequest(String transcribedText) {
        return contextFactory.generatePlayerInstructions(String.valueOf(transcribedText));
    }

    private String buildSystemRequest(String systemInput) {
        return contextFactory.generateSystemInstructions(systemInput);
    }

/*    private static HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL("https://api.x.ai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY));
        conn.setDoOutput(true);
        return conn;
    }*/

    // Debug string to reveal control characters
    private String toDebugString(String input) {
        if (input == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c < 32 || c == 127 || c == 0xFEFF) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Enhanced JSON escaping for plain strings
    private String escapeJson(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c < 32 || c == 127 || c == 0xFEFF) {
                sb.append(' '); // Replace control characters
            } else if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
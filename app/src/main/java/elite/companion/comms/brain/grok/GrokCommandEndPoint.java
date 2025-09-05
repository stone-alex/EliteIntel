package elite.companion.comms.brain.grok;

import com.google.common.eventbus.Subscribe;
import com.google.gson.*;
import elite.companion.comms.brain.AIChatInterface;
import elite.companion.comms.brain.AIContextFactory;
import elite.companion.comms.brain.AIRouterInterface;
import elite.companion.comms.brain.AiCommandInterface;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.UserInputEvent;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.ConfigManager;
import elite.companion.util.EventBusManager;
import elite.companion.util.GsonFactory;
import elite.companion.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GrokCommandEndPoint implements AiCommandInterface {
    private static final Logger log = LoggerFactory.getLogger(GrokCommandEndPoint.class);

    private static final ThreadLocal<JsonArray> currentHistory = new ThreadLocal<>();
    private final AIRouterInterface router;
    private final AIChatInterface chatInterface;

    public GrokCommandEndPoint() {
        this.router = GrokResponseRouter.getInstance();
        this.chatInterface = GrokChatEndPoint.getInstance();
        EventBusManager.register(this);
    }


    @Override public void start() throws Exception {
        router.start();
        log.info("Started GrokInteractionHandler");
    }

    @Override public void stop() {
        router.stop();
        log.info("Stopped GrokInteractionHandler");
    }

    @Subscribe @Override public void onUserInput(UserInputEvent event) {
        processVoiceCommand(event.getUserInput(), event.getConfidence());
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
            String systemPrompt = AIContextFactory.getInstance().generateSystemPrompt();
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
            clarification.addProperty("response_text", "Sorry, missed that. Say again?");
            clarification.addProperty("action", (String) null);
            clarification.add("params", new JsonObject());
            clarification.addProperty("expect_followup", true);

            // Route clarification without direct TTS
            router.processAiResponse(clarification, userInput);

            // Store user message in history for follow-up
            JsonObject assistantMessage = new JsonObject();
            assistantMessage.addProperty("role", "assistant");
            assistantMessage.addProperty("content", "Sorry, missed that. Say again?");
            SystemSession.getInstance().appendToChatHistory(userMessage, assistantMessage);
            return;
        }

        // Log sanitized input
        log.info("Sanitized voice userInput: [{}] (confidence: {})", toDebugString(userInput), confidence);

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        String systemPrompt = AIContextFactory.getInstance().generateSystemPrompt();
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
        String input = event.getSensorData();


        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        String systemPrompt = AIContextFactory.getInstance().generateSystemPrompt();
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", buildSystemRequest(input));
        messages.add(userMessage);

        // Create API request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "grok-3-fast");
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("stream", false);
        requestBody.add("messages", messages);

        // Serialize to JSON string
        Gson gson = GsonFactory.getGson();
        String jsonString = gson.toJson(requestBody);
        log.debug("JSON prepared for callXaiApi: [{}]", toDebugString(jsonString));

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
    }

    private JsonObject callXaiApi(String jsonString) {
        try {
            HttpURLConnection conn = getHttpURLConnection();

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
        return AIContextFactory.getInstance().generatePlayerInstructions(String.valueOf(transcribedText));
    }

    private String buildSystemRequest(String systemInput) {
        return AIContextFactory.getInstance().generateSystemInstructions(systemInput);
    }

    private static HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL("https://api.x.ai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ConfigManager.getInstance().getSystemKey(ConfigManager.GROK_API_KEY));
        conn.setDoOutput(true);
        return conn;
    }

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
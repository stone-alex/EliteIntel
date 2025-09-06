package elite.companion.comms.brain.grok;

import com.google.gson.*;
import elite.companion.comms.ApiFactory;
import elite.companion.comms.ConfigManager;
import elite.companion.comms.brain.AiAnalysisInterface;
import elite.companion.util.json.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GrokAnalysisEndpoint implements AiAnalysisInterface {
    private static final Logger logger = LoggerFactory.getLogger(GrokAnalysisEndpoint.class);
    private final String apiUrl = "https://api.x.ai/v1/chat/completions";
    private final Gson gson = GsonFactory.getGson();
    private static final GrokAnalysisEndpoint instance = new GrokAnalysisEndpoint();

    private GrokAnalysisEndpoint() {
    }

    public static GrokAnalysisEndpoint getInstance() {
        return instance;
    }

    @Override public JsonObject analyzeData(String userIntent, String dataJson) {
        try {
            HttpURLConnection conn = getHttpURLConnection();
            String systemPrompt = ApiFactory.getInstance().getAiContextFactory().generateAnalysisPrompt(userIntent, dataJson);

            JsonObject request = new JsonObject();
            request.addProperty("model", "grok-3-fast");
            request.addProperty("temperature", 0.7);
            request.addProperty("stream", false);

            JsonObject messageSystem = new JsonObject();
            messageSystem.addProperty("role", "system");
            messageSystem.addProperty("content", systemPrompt);

            JsonObject messageUser = new JsonObject();
            messageUser.addProperty("role", "user");
            messageUser.addProperty("content", "User intent: " + userIntent + "\nData: " + dataJson);

            request.add("messages", gson.toJsonTree(new Object[]{messageSystem, messageUser}));

            String jsonString = gson.toJson(request);
            logger.debug("xAI API call: [{}]", toDebugString(jsonString));

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
                logger.info("Stripped BOM from response");
            }

            logger.debug("xAI API response: [{}]", toDebugString(response));

            if (responseCode != 200) {
                String errorResponse = "";
                try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                    errorResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                } catch (Exception e) {
                    logger.warn("Failed to read error stream: {}", e.getMessage());
                }
                logger.error("xAI API error: {} - {}", responseCode, conn.getResponseMessage());
                logger.info("Error response body: {}", errorResponse);
                return createErrorResponse("Analysis error.");
            }

            // Parse response
            JsonObject json;
            try {
                json = JsonParser.parseString(response).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.error("Failed to parse API response: [{}]", toDebugString(response), e);
                return createErrorResponse("Analysis error.");
            }

            // Extract content safely
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                logger.error("No choices in API response: [{}]", toDebugString(response));
                return createErrorResponse("Analysis error.");
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                logger.error("No message in API response choices: [{}]", toDebugString(response));
                return createErrorResponse("Analysis error.");
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                logger.error("No content in API response message: [{}]", toDebugString(response));
                return createErrorResponse("Analysis error.");
            }

            logger.debug("API response content: [{}]", toDebugString(content));

            // Extract JSON from content
            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                jsonStart = content.indexOf("{");
                if (jsonStart == -1) {
                    logger.error("No JSON object found in content: [{}]", toDebugString(content));
                    return createErrorResponse("Analysis error.");
                }
                jsonContent = content.substring(jsonStart);
            }

            logger.info("Extracted JSON content: [{}]", toDebugString(jsonContent));

            // Parse extracted JSON content
            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.error("Failed to parse API response content: [{}]", toDebugString(jsonContent), e);
                return createErrorResponse("Analysis error.");
            }
        } catch (Exception e) {
            logger.error("AI API call fatal error: {}", e.getMessage(), e);
            return createErrorResponse("Analysis error. Check logs.");
        }
    }

    private JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("response_text", message);
        return error;
    }

    private HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY));
        conn.setDoOutput(true);
        return conn;
    }

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
}
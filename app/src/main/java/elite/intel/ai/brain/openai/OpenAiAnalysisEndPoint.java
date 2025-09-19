package elite.intel.ai.brain.openai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiAnalysisInterface;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class OpenAiAnalysisEndPoint implements AiAnalysisInterface {
    private static final Logger logger = LogManager.getLogger(OpenAiAnalysisEndPoint.class);
    private static OpenAiAnalysisEndPoint instance;

    private OpenAiAnalysisEndPoint() {
    }

    public static synchronized OpenAiAnalysisEndPoint getInstance() {
        if (instance == null) {
            instance = new OpenAiAnalysisEndPoint();
        }
        return instance;
    }

    @Override
    public JsonObject analyzeData(String userIntent, String dataJson) {
        try {
            OpenAiClient client = OpenAiClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();
            String systemPrompt = ApiFactory.getInstance().getAiContextFactory().generateAnalysisPrompt(userIntent, dataJson);

            JsonObject request = client.createRequestBodyHeader(OpenAiClient.MODEL);

            JsonObject messageSystem = new JsonObject();
            messageSystem.addProperty("role", AIConstants.ROLE_SYSTEM);
            messageSystem.addProperty("content", systemPrompt);

            JsonObject messageUser = new JsonObject();
            messageUser.addProperty("role", AIConstants.ROLE_USER);
            messageUser.addProperty("content", "User intent: " + userIntent + "\nData: " + dataJson);

            JsonArray messages = new JsonArray();
            messages.add(messageSystem);
            messages.add(messageUser);
            request.add("messages", messages);

            String jsonString = GsonFactory.getGson().toJson(request);
            logger.debug("Open AI API call: [{}]", toDebugString(jsonString));

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

            logger.debug("Open AI API response: [{}]", toDebugString(response));

            if (responseCode != 200) {
                String errorResponse = "";
                try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                    errorResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                } catch (Exception e) {
                    logger.warn("Failed to read error stream: {}", e.getMessage());
                }
                logger.error("Open AI API error: {} - {}", responseCode, conn.getResponseMessage());
                logger.info("Error response body: {}", errorResponse);
                return client.createErrorResponse("Analysis error.");
            }

            // Parse response
            JsonObject json;
            try {
                json = JsonParser.parseString(response).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.error("Failed to parse API response: [{}]", toDebugString(response), e);
                return client.createErrorResponse("Analysis error.");
            }

            // Extract content safely
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                logger.error("No choices in API response: [{}]", toDebugString(response));
                return client.createErrorResponse("Analysis error.");
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                logger.error("No message in API response choices: [{}]", toDebugString(response));
                return client.createErrorResponse("Analysis error.");
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                logger.error("No content in API response message: [{}]", toDebugString(response));
                return client.createErrorResponse("Analysis error.");
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
                    return client.createErrorResponse("Analysis error.");
                }
                jsonContent = content.substring(jsonStart);
            }

            logger.info("Extracted JSON content: [{}]", toDebugString(jsonContent));

            // Parse extracted JSON content
            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.error("Failed to parse API response content: [{}]", toDebugString(jsonContent), e);
                return client.createErrorResponse("Analysis error.");
            }
        } catch (IOException e) {
            logger.error("Open AI API call failed: {}", e.getMessage(), e);
            return OpenAiClient.getInstance().createErrorResponse("Analysis error. Check logs.");
        } catch (Exception e) {
            logger.error("AI API call fatal error: {}", e.getMessage(), e);
            return OpenAiClient.getInstance().createErrorResponse("Analysis error. Check logs.");
        }
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
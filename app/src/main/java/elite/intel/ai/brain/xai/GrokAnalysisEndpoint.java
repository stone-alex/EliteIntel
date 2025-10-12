package elite.intel.ai.brain.xai;

import com.google.gson.*;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiAnalysisInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * The GrokAnalysisEndpoint class provides functionality for analyzing user-provided data using
 * an AI-based service. It implements the AiAnalysisInterface and communicates with an external
 * API to process user intents and data for generating analysis results.
 * <p>
 * This class is designed as a singleton, ensuring only one instance is created and utilized
 * throughout the application. It makes HTTP requests to an AI endpoint to analyze the input,
 * processes the response, and extracts relevant content as a JSON object.
 */
public class GrokAnalysisEndpoint extends AiEndPoint implements AiAnalysisInterface {
    private static final Logger logger = LogManager.getLogger(GrokAnalysisEndpoint.class);
    private final Gson gson = GsonFactory.getGson();
    private static final GrokAnalysisEndpoint instance = new GrokAnalysisEndpoint();

    private GrokAnalysisEndpoint() {
    }

    public static GrokAnalysisEndpoint getInstance() {
        return instance;
    }

    @Override public JsonObject analyzeData(String userIntent, String dataJson) {
        try {
            GrokClient client = GrokClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();
            String systemPrompt = ApiFactory.getInstance().getAiPromptFactory().generateAnalysisPrompt(userIntent, dataJson);

            JsonObject request = client.createRequestBodyHeader(GrokClient.MODEL_GROK_4_FAST_REASONING, 1);

            JsonObject messageSystem = new JsonObject();
            messageSystem.addProperty("role", AIConstants.ROLE_SYSTEM);
            messageSystem.addProperty("content", systemPrompt);

            JsonObject messageUser = new JsonObject();
            messageUser.addProperty("role", AIConstants.ROLE_USER);
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
        } catch (Exception e) {
            logger.error("AI API call fatal error: {}", e.getMessage(), e);
            return GrokClient.getInstance().createErrorResponse("Analysis error. Check logs.");
        }
    }
}
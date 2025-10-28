package elite.intel.ai.brain.openai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.Client;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class OpenAiChatEndPoint extends AiEndPoint implements AIChatInterface {
    private static final Logger log = LogManager.getLogger(OpenAiChatEndPoint.class);
    private static OpenAiChatEndPoint instance;

    private OpenAiChatEndPoint() {
    }

    public static synchronized OpenAiChatEndPoint getInstance() {
        if (instance == null) {
            instance = new OpenAiChatEndPoint();
        }
        return instance;
    }

    @Override
    public JsonObject sendToAi(JsonArray messages) {
        String jsonString = null;
        try {
            // Sanitize messages
            JsonArray sanitizedMessages = sanitizeJsonArray(messages);

            Client client = OpenAiClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();

            JsonObject body = client.createRequestBodyHeader(OpenAiClient.MODEL_GPT_4_1_MINI, 1);
            body.add("messages", sanitizedMessages);

            jsonString = GsonFactory.getGson().toJson(body);
            log.info("Open AI API chat call:\n\n{}\n\n", jsonString);

            Response response = callApi(conn, jsonString, client);

            // Extract content safely
            JsonArray choices = response.responseData().getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("No choices in API response:\n{}", response.responseMessage());
                return null;
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                log.error("No message in API response choices:\n{}", response.responseMessage());
                return null;
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                log.error("No content in API response message:\n{}", response.responseMessage());
                return null;
            }

            // Log content before parsing
            log.info("API response content:\n\n{}\n\n", content);

            // Extract JSON from content (after double newline or first valid JSON object)
            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                // Fallback: If no JSON, assume chat response
                jsonStart = content.indexOf("{");
                if (jsonStart == -1) {
                    JsonObject result = new JsonObject();
                    result.addProperty("type", AIConstants.TYPE_CHAT);
                    result.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, content);
                    return result;
                }
                jsonContent = content.substring(jsonStart);
                // Validate JSON
                try {
                    JsonParser.parseString(jsonContent);
                } catch (JsonSyntaxException e) {
                    log.error("Invalid JSON object in content:\n{}", jsonContent, e);
                    JsonObject result = new JsonObject();
                    result.addProperty("type", AIConstants.TYPE_CHAT);
                    result.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, content);
                    return result;
                }
            }

            // Log extracted JSON
            log.info("Extracted JSON content:\n\n{}\n\n", GsonFactory.getGson().toJson(jsonContent));

            // Parse JSON content
            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content:\n\n{}\n\n", GsonFactory.getGson().toJson(jsonContent), e);
                JsonObject result = new JsonObject();
                result.addProperty("type", AIConstants.TYPE_CHAT);
                result.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, content);
                return result;
            }
        } catch (Exception e) {
            log.error("Open AI API chat call fatal error: {}", e.getMessage(), e);
            log.error("Input data:\n{}", jsonString != null ? jsonString : "null");
            return null;
        }
    }
}
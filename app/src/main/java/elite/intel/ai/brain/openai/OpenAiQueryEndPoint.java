package elite.intel.ai.brain.openai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.AiQueryInterface;
import elite.intel.ai.brain.Client;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class OpenAiQueryEndPoint extends AiEndPoint implements AiQueryInterface {
    private static final Logger log = LogManager.getLogger(OpenAiQueryEndPoint.class);
    private static OpenAiQueryEndPoint instance;

    private OpenAiQueryEndPoint() {
    }

    public static synchronized OpenAiQueryEndPoint getInstance() {
        if (instance == null) {
            instance = new OpenAiQueryEndPoint();
        }
        return instance;
    }

    @Override
    public JsonObject processAiPrompt(JsonArray messages) {
        String jsonString = null;
        try {
            // Sanitize messages
            JsonArray sanitizedMessages = sanitizeJsonArray(messages);

            Client client = OpenAiClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();

            JsonObject promp = client.createPrompt(OpenAiClient.MODEL_GPT_4_1_MINI, 1);
            promp.add("messages", sanitizedMessages);

            jsonString = GsonFactory.getGson().toJson(promp);
            log.debug("Open AI API query call:\n\n{}\n\n", jsonString);



            Response response = processAiPrompt(conn, jsonString, client);

            // Extract content safely
            JsonArray choices = response.responseData().getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("No choices in API response:\n{}", response);
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
            log.debug("API response content:\n\n{}\n\n", content);

            // Extract JSON from content (after double newline or first valid JSON object)
            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                // Fallback: Find first { that starts a valid JSON object
                jsonStart = content.indexOf("{");
                if (jsonStart == -1) {
                    log.error("No JSON object found in content:\n{}", content);
                    return null;
                }
                jsonContent = content.substring(jsonStart);
                // Validate JSON
                try {
                    JsonParser.parseString(jsonContent);
                } catch (JsonSyntaxException e) {
                    log.error("Invalid JSON object in content:\n\n{}\n\n", GsonFactory.getGson().toJson(jsonContent), e);
                    return null;
                }
            }

            // Log extracted JSON
            log.debug("Extracted JSON content:\n\n{}\n\n", GsonFactory.getGson().toJson(jsonContent));

            // Parse JSON content
            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content:\n{}", GsonFactory.getGson().toJson(jsonContent), e);
                return null;
            }
        } catch (Exception e) {
            log.error("Open AI API query call fatal error: {}", e.getMessage(), e);
            log.error("Input data:\n{}", jsonString != null ? jsonString : "null");
            return null;
        }
    }
}
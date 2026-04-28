package elite.intel.ai.brain.deepseek;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.commons.AiEndPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeepSeekChatEndPoint extends AiEndPoint implements AIChatInterface {
    private static final Logger log = LogManager.getLogger(DeepSeekChatEndPoint.class);
    private static final DeepSeekChatEndPoint INSTANCE = new DeepSeekChatEndPoint();

    private DeepSeekChatEndPoint() {
    }

    public static DeepSeekChatEndPoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject processAiPrompt(JsonArray messages, float temp) {
        String bodyString = null;
        try {
            DeepSeekClient client = DeepSeekClient.getInstance();
            JsonObject prompt = client.createPrompt(DeepSeekClient.MODEL, temp);

            JsonArray sanitizedMessages = sanitizeJsonArray(messages);
            prompt.add("messages", sanitizedMessages);

            bodyString = prompt.toString();
            log.debug("DeepSeek API chat call:\n{}", bodyString);

            JsonObject response = processAiPrompt(bodyString, client);

            JsonArray choices = response.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("No choices in API response:\n{}", response);
                return null;
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                log.error("No message in API response choices:\n{}", response);
                return null;
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                log.error("No content in API response message:\n{}", response);
                return null;
            }

            log.debug("API response content:\n{}", content);

            String jsonContent = extractJsonFromContent(content);
            if (jsonContent == null) {
                JsonObject result = new JsonObject();
                result.addProperty(AIConstants.PROPERTY_TEXT_TO_SPEECH_RESPONSE, content);
                return result;
            }

            log.debug("Extracted JSON content:\n\n{}\n\n", jsonContent);

            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content:\n{}", jsonContent, e);
                JsonObject result = new JsonObject();
                result.addProperty(AIConstants.PROPERTY_TEXT_TO_SPEECH_RESPONSE, content);
                return result;
            }
        } catch (Exception e) {
            log.error("DeepSeek API chat call fatal error: {}", e.getMessage(), e);
            log.error("Input data:\n{}", bodyString != null ? bodyString : "null");
            return null;
        }
    }
}

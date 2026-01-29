package elite.intel.ai.brain.xai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.session.SystemSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

/**
 * Singleton class representing an AI chat communication endpoint with Grok.
 * Implements the AIChatInterface to provide functionality for sending message history
 * and receiving AI-generated responses.
 */
public class GrokChatEndPoint extends AiEndPoint implements AIChatInterface {
    private static final Logger log = LogManager.getLogger(GrokChatEndPoint.class);
    private static final GrokChatEndPoint INSTANCE = new GrokChatEndPoint();

    private GrokChatEndPoint() {
        // Private constructor for singleton
    }

    public static GrokChatEndPoint getInstance() {
        return INSTANCE;
    }

    /**
     * Sends a full message history to Grok for chat continuation.
     * The first message should be the system prompt (role: system).
     * Returns the parsed JSON response content or null on error.
     */
    @Override public JsonObject processAiPrompt(JsonArray messages) {
        String bodyString = null;
        try {
            GrokClient client = GrokClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();
            JsonObject prompt = client.createPrompt(GrokClient.MODEL_GROK_REASONING, 1);

            // Sanitize messages
            JsonArray sanitizedMessages = sanitizeJsonArray(messages);
            prompt.add("messages", sanitizedMessages);

            bodyString = prompt.toString();
            log.debug("xAI API chat call:\n{}", bodyString);


            JsonObject response = processAiPrompt(bodyString, client);

            // Extract content safely
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

            // Log content before parsing
            log.debug("API response content:\n{}", content);

            // Extract JSON from content (after double newline or first valid JSON object)
            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                // Fallback: This always have a chat response. So we route it to chat.
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
                    return null;
                }
            }

            // Log extracted JSON
            log.debug("Extracted JSON content:\n\n{}\n\n", jsonContent);

            // Parse JSON content
            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content:\n{}", jsonContent, e);
                throw e;
            }
        } catch (Exception e) {
            log.error("AI API chat call fatal error: {}", e.getMessage(), e);
            log.error("Input data:\n{}", bodyString != null ? bodyString : "null");
            return null;
        }
    }
}
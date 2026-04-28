package elite.intel.ai.brain.inference.gemini;

import com.google.gson.*;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.commons.AiEndPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton chat endpoint for Google Gemini.
 * Converts the internal messages format (role/content pairs) into Gemini's
 * request shape: system messages → system_instruction, user messages → contents[].parts[].
 */
public class GeminiChatEndPoint extends AiEndPoint implements AIChatInterface {
    private static final Logger log = LogManager.getLogger(GeminiChatEndPoint.class);
    private static final GeminiChatEndPoint INSTANCE = new GeminiChatEndPoint();

    private GeminiChatEndPoint() {
    }

    public static GeminiChatEndPoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject processAiPrompt(JsonArray messages, float temp) {
        String bodyString = null;
        try {
            GeminiClient client = GeminiClient.getInstance();
            JsonObject prompt = client.createPrompt(GeminiClient.MODEL_FLASH, temp);

            // Separate system messages from user/model messages
            StringBuilder systemContent = new StringBuilder();
            JsonArray contents = new JsonArray();

            JsonArray sanitized = sanitizeJsonArray(messages);
            for (JsonElement element : sanitized) {
                JsonObject msg = element.getAsJsonObject();
                String role = msg.get("role").getAsString();
                String content = msg.get("content").getAsString();

                if (AIConstants.ROLE_SYSTEM.equals(role)) {
                    if (!systemContent.isEmpty()) systemContent.append("\n\n");
                    systemContent.append(content);
                } else {
                    // Gemini uses "model" instead of "assistant"
                    String geminiRole = "assistant".equals(role) ? "model" : role;
                    JsonObject geminiMsg = new JsonObject();
                    geminiMsg.addProperty("role", geminiRole);
                    JsonArray parts = new JsonArray();
                    JsonObject part = new JsonObject();
                    part.addProperty("text", content);
                    parts.add(part);
                    geminiMsg.add("parts", parts);
                    contents.add(geminiMsg);
                }
            }

            if (!systemContent.isEmpty()) {
                JsonObject systemInstruction = new JsonObject();
                JsonArray parts = new JsonArray();
                JsonObject part = new JsonObject();
                part.addProperty("text", systemContent.toString());
                parts.add(part);
                systemInstruction.add("parts", parts);
                prompt.add("system_instruction", systemInstruction);
            }

            prompt.add("contents", contents);
            bodyString = prompt.toString();
            log.debug("Gemini API chat call:\n{}", bodyString);

            JsonObject rawResponse = processAiPrompt(bodyString, client);
            if (rawResponse == null) {
                log.error("Null response from Gemini API");
                return null;
            }

            if (!rawResponse.has("candidates")) {
                log.error("No candidates in Gemini response:\n{}", rawResponse);
                JsonObject fallback = new JsonObject();
                fallback.addProperty(AIConstants.PROPERTY_TEXT_TO_SPEECH_RESPONSE, "Gemini did not return any candidates. LLM Code aborted");
                return fallback;
            }

            String text = client.extractText(rawResponse);
            log.debug("Gemini raw text response:\n{}", text);

            // Strip markdown code fences if present
            text = extractJson(text);

            try {
                return JsonParser.parseString(text).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                // Wrap plain text as TTS response
                JsonObject fallback = new JsonObject();
                fallback.addProperty(AIConstants.PROPERTY_TEXT_TO_SPEECH_RESPONSE, text);
                return fallback;
            }

        } catch (Exception e) {
            log.error("Gemini API chat call fatal error: {}", e.getMessage(), e);
            log.error("Input data:\n{}", bodyString != null ? bodyString : "null");
            return null;
        }
    }

    /**
     * Extracts the first valid JSON object from text, stripping markdown fences if present.
     */
    private String extractJson(String text) {
        if (text == null) return "{}";
        text = text.trim();
        int fenceStart = text.indexOf("```json");
        if (fenceStart != -1) {
            int start = text.indexOf('{', fenceStart + 7);
            int end = text.lastIndexOf('}');
            if (start != -1 && end > start) return text.substring(start, end + 1);
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end > start) return text.substring(start, end + 1);
        return text;
    }
}

package elite.intel.ai.brain.anthropic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnthropicUserEndPoint extends AiEndPoint implements AIChatInterface {

    private static final Logger log = LogManager.getLogger(AnthropicUserEndPoint.class);
    private static final AnthropicUserEndPoint INSTANCE = new AnthropicUserEndPoint();

    private AnthropicUserEndPoint() {
    }

    public static AnthropicUserEndPoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject processAiPrompt(JsonArray messages, float temp) {
        String bodyString = null;
        try {
            AnthropicClient client = AnthropicClient.getInstance();

            // Build base prompt object (model, max_tokens, temperature)
            JsonObject prompt = client.createPrompt(AnthropicClient.MODEL_COMMAND_PROMPT, temp);

            // ── Separate system messages from user/assistant messages ──────────
            StringBuilder systemContent = new StringBuilder();
            JsonArray conversationMessages = new JsonArray();

            JsonArray sanitized = sanitizeJsonArray(messages);
            for (JsonElement element : sanitized) {
                JsonObject msg = element.getAsJsonObject();
                String role = msg.has("role") ? msg.get("role").getAsString() : "";

                if (AIConstants.ROLE_SYSTEM.equals(role)) {
                    if (!systemContent.isEmpty()) systemContent.append("\n\n");
                    systemContent.append(msg.get("content").getAsString());
                } else {
                    conversationMessages.add(msg);
                }
            }

            // Claude requires at least one user message
            if (conversationMessages.isEmpty()) {
                log.error("No user/assistant messages found after system extraction");
                return null;
            }

            if (!systemContent.isEmpty()) {
                JsonArray systemArray = new JsonArray();
                JsonObject systemBlock = new JsonObject();
                systemBlock.addProperty("type", "text");
                systemBlock.addProperty("text", systemContent.toString());
                JsonObject cacheControl = new JsonObject();
                cacheControl.addProperty("type", "ephemeral");
                systemBlock.add("cache_control", cacheControl);
                systemArray.add(systemBlock);
                prompt.add("system", systemArray);
            }
            prompt.add("messages", conversationMessages);


            bodyString = prompt.toString();
            log.debug("Anthropic API call:\n{}", GsonFactory.getGson().toJson(prompt));

            // ── Send and unwrap ────────────────────────────────────────────────
            JsonObject root = processAiPrompt(bodyString, client);
            if (root == null) {
                log.error("Null response from Anthropic API");
                return null;
            }

            log.debug("Anthropic raw response:\n{}", GsonFactory.getGson().toJson(root));

            // Check for API-level error envelope  { "type": "error", "error": {...} }
            if ("error".equals(getStringOrNull(root, "type"))) {
                JsonObject apiErr = root.has("error") ? root.getAsJsonObject("error") : root;
                log.error("Anthropic API error: {}", apiErr);
                return null;
            }

            // Extract text from content[0].text
            String text = client.extractText(root);
            if (text == null || text.isBlank()) {
                log.error("Empty content in Anthropic response:\n{}", root);
                return null;
            }

            // The shared prompt factory instructs all LLMs to reply with JSON.
            // Strip any accidental markdown fences Claude may add.
            text = stripJsonFences(text);

            return JsonParser.parseString(text).getAsJsonObject();

        } catch (Exception e) {
            log.error("Anthropic chat call failed: {}", e.getMessage(), e);
            log.error("Request body was:\n{}", bodyString != null ? bodyString : "null");
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Strip ```json … ``` or ``` … ``` fences that Claude occasionally wraps around JSON.
     */
    private String stripJsonFences(String text) {
        String t = text.trim();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline != -1) t = t.substring(firstNewline + 1);
            if (t.endsWith("```")) t = t.substring(0, t.lastIndexOf("```"));
            t = t.trim();
        }
        return t;
    }

    private String getStringOrNull(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : null;
    }
}
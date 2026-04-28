package elite.intel.ai.brain.inference.anthropic;

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

import java.util.ArrayList;
import java.util.List;

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
            JsonObject prompt = client.createPrompt(AnthropicClient.MODEL_COMMAND_MODEL, temp);

            // -- Separate system messages from user/assistant messages ----------
            List<String> systemParts = new ArrayList<>();
            JsonArray conversationMessages = new JsonArray();

            JsonArray sanitized = sanitizeJsonArray(messages);
            for (JsonElement element : sanitized) {
                JsonObject msg = element.getAsJsonObject();
                String role = msg.has("role") ? msg.get("role").getAsString() : "";

                if (AIConstants.ROLE_SYSTEM.equals(role)) {
                    systemParts.add(msg.get("content").getAsString());
                } else {
                    conversationMessages.add(msg);
                }
            }

            // Claude requires at least one user message
            if (conversationMessages.isEmpty()) {
                log.error("No user/assistant messages found after system extraction");
                return null;
            }

            if (!systemParts.isEmpty()) {
                JsonArray systemArray = new JsonArray();
                if (systemParts.size() == 1) {
                    // Single block (chat / sensor with one system msg) to cache all of it
                    addSystemBlock(systemArray, systemParts.get(0), true);
                } else {
                    // Multiple blocks: combine first N-1 into one cached block (static rules),
                    // leave the last block uncached (dynamic Reducer action list or event instructions).
                    StringBuilder cached = new StringBuilder();
                    for (int i = 0; i < systemParts.size() - 1; i++) {
                        if (!cached.isEmpty()) cached.append("\n\n");
                        cached.append(systemParts.get(i));
                    }
                    addSystemBlock(systemArray, cached.toString(), true);
                    addSystemBlock(systemArray, systemParts.get(systemParts.size() - 1), false);
                }
                prompt.add("system", systemArray);
            }
            prompt.add("messages", conversationMessages);


            bodyString = prompt.toString();
            log.debug("Anthropic API call:\n{}", GsonFactory.getGson().toJson(prompt));

            // -- Send and unwrap ------------------------------------------------
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

    private void addSystemBlock(JsonArray arr, String text, boolean cache) {
        JsonObject block = new JsonObject();
        block.addProperty("type", "text");
        block.addProperty("text", text);
        if (cache) {
            JsonObject cc = new JsonObject();
            cc.addProperty("type", "ephemeral");
            block.add("cache_control", cc);
        }
        arr.add(block);
    }

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
package elite.intel.ai.brain.ollama;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OllamaCommandEndPoint extends AiEndPoint implements AIChatInterface {

    private static final Logger log = LogManager.getLogger(OllamaCommandEndPoint.class);
    private static final OllamaCommandEndPoint INSTANCE = new OllamaCommandEndPoint();

    private OllamaCommandEndPoint() {
    }

    public static OllamaCommandEndPoint getInstance() {
        return INSTANCE;
    }
    @Override public JsonObject processAiPrompt(JsonArray messages, float temp) {
        String bodyString = null;
        try {
            OllamaClient client = OllamaClient.getInstance();
            JsonObject prompt = client.createPrompt(OllamaClient.MODEL_OLLAMA_SMALL, temp);

            JsonArray sanitized = sanitizeJsonArray(messages);
            prompt.add("messages", sanitized);

            // === ADD STRUCTURED SCHEMA ENFORCEMENT ===
            JsonObject format = new JsonObject();
            JsonObject properties = new JsonObject();

            JsonObject responseTextProp = new JsonObject();
            responseTextProp.addProperty("type", "string");
            properties.add("response_text", responseTextProp);

            JsonObject actionProp = new JsonObject();
            actionProp.addProperty("type", "string");
            properties.add("action", actionProp);

            JsonObject paramsProp = new JsonObject();
            paramsProp.addProperty("type", "object");
            properties.add("params", paramsProp);

            JsonObject expectProp = new JsonObject();
            expectProp.addProperty("type", "boolean");
            properties.add("expect_followup", expectProp);

            JsonArray required = new JsonArray();
            required.add("type"); required.add("response_text"); required.add("action");
            required.add("params"); required.add("expect_followup");
            format.add("required", required);
            format.addProperty("additionalProperties", false);
            format.addProperty("type", "object");
            JsonObject options = new JsonObject();
            options.add("format", format);
            options.addProperty("raw", true);
            prompt.add("options", options);

            bodyString = prompt.toString();
            log.debug("Ollama API call:\n{}", GsonFactory.getGson().toJson(prompt));

            JsonObject root = processAiPrompt(bodyString, client);

            JsonObject message = root.getAsJsonObject("message");
            if (message == null || !message.has("content")) {
                log.error("No message/content from Ollama:\n{}", root);
                return null;
            }

            String content = message.get("content").getAsString();
            log.debug("Ollama raw response:\n{}", GsonFactory.getGson().toJson(message));

            // Now content is already your exact schema JSON
            return JsonParser.parseString(content).getAsJsonObject();

        } catch (Exception e) {
            log.error("Ollama chat call failed: {}", e.getMessage(), e);
            log.error("Request body was:\n{}", bodyString != null ? bodyString : "null");
            return null;
        }
    }
}
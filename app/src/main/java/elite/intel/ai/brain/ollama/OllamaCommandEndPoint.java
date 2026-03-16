package elite.intel.ai.brain.ollama;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonUtils;
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
            JsonObject prompt = client.createPrompt(OllamaClient.MODEL_COMMANDS, temp);

            JsonArray sanitized = sanitizeJsonArray(messages);
            prompt.add("messages", sanitized);

            // === STRUCTURED SCHEMA ENFORCEMENT ===
            // Command parser returns: type, action, params - never response_text
            JsonObject format = new JsonObject();
            JsonObject properties = new JsonObject();

            JsonObject typeProp = new JsonObject();
            typeProp.addProperty("type", "string");
            properties.add("type", typeProp);

            JsonObject actionProp = new JsonObject();
            actionProp.addProperty("type", "string");
            properties.add("action", actionProp);

            JsonObject paramsProp = new JsonObject();
            paramsProp.addProperty("type", "object");
            properties.add("params", paramsProp);

            format.add("properties", properties);


            // Only enforce the fields the command parser actually produces
            JsonArray required = new JsonArray();
            required.add("type");
            required.add("action");
            required.add("params");
            format.add("required", required);
            format.addProperty("additionalProperties", false);
            format.addProperty("type", "object");

            // format must be top-level per Ollama structured output API (not inside options)
            prompt.add("format", format);

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

            return JsonParser.parseString(JsonUtils.repairLlmJson(content)).getAsJsonObject();

        } catch (Exception e) {
            log.error("Ollama chat call failed: {}", e.getMessage(), e);
            log.error("Request body was:\n{}", bodyString != null ? bodyString : "null");
            return null;
        }
    }
}
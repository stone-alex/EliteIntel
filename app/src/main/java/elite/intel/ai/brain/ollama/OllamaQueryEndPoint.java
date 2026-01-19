package elite.intel.ai.brain.ollama;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiQueryInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OllamaQueryEndPoint extends AiEndPoint implements AiQueryInterface {
    private static final Logger log = LogManager.getLogger(OllamaQueryEndPoint.class);
    private static final OllamaQueryEndPoint INSTANCE = new OllamaQueryEndPoint();

    private OllamaQueryEndPoint() {
    }

    public static OllamaQueryEndPoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject sendToAi(JsonArray messages) {
        try {
            OllamaClient client = OllamaClient.getInstance();
            var conn = client.getHttpURLConnection();

            JsonObject request = client.createRequestBodyHeader(OllamaClient.MODEL_OLLAMA, 0.9f);
            request.add("messages", sanitizeJsonArray(messages));

            log.debug("Ollama query call:\n{}", request);
            Response response = callApi(conn, request.toString(), client);

            JsonObject root = response.responseData();
            String content = root.getAsJsonObject("message").get("content").getAsString();

            log.debug("Ollama raw response:\n{}", content);
            try {
                return JsonParser.parseString(content).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException text) {
                log.error("Failed to parse Ollama response JSON: {}", text.getMessage());
                JsonObject result = new JsonObject();
                result.addProperty("action", AIConstants.TYPE_CHAT);
                result.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, content);
                return result;
            }

        } catch (Exception e) {
            log.error("Ollama query failed", e);
            return null;
        }
    }
}
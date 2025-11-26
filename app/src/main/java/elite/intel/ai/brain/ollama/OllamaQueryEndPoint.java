package elite.intel.ai.brain.ollama;

import com.google.gson.*;
import elite.intel.ai.brain.AiQueryInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OllamaQueryEndPoint extends AiEndPoint implements AiQueryInterface {
    private static final Logger log = LogManager.getLogger(OllamaQueryEndPoint.class);
    private static final OllamaQueryEndPoint INSTANCE = new OllamaQueryEndPoint();

    private OllamaQueryEndPoint() {}

    public static OllamaQueryEndPoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject sendToAi(JsonArray messages) {
        try {
            OllamaClient client = OllamaClient.getInstance();
            var conn = client.getHttpURLConnection();

            JsonObject body = client.createRequestBodyHeader(OllamaClient.MODEL_TINYLAMA, 0.7f);
            body.add("messages", sanitizeJsonArray(messages));

            log.debug("Ollama query call:\n{}", body);
            Response response = callApi(conn, body.toString(), client);

            JsonObject root = response.responseData();
            String content = root.getAsJsonObject("message").get("content").getAsString();

            log.debug("Ollama raw response:\n{}", content);
            return JsonParser.parseString(content).getAsJsonObject();

        } catch (Exception e) {
            log.error("Ollama query failed", e);
            return null;
        }
    }
}
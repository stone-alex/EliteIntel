package elite.intel.ai.brain.ollama;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class OllamaChatEndPoint extends AiEndPoint implements AIChatInterface {

    private static final Logger log = LogManager.getLogger(OllamaChatEndPoint.class);
    private static final OllamaChatEndPoint INSTANCE = new OllamaChatEndPoint();

    private OllamaChatEndPoint() {
    }

    public static OllamaChatEndPoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject sendToAi(JsonArray messages) {
        String bodyString = null;
        try {
            OllamaClient client = OllamaClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();

            JsonObject body = client.createRequestBodyHeader(OllamaClient.MODEL_PFI_MINI, 1.0f);

            JsonArray sanitized = sanitizeJsonArray(messages);
            body.add("messages", sanitized);
            body.addProperty("stream", false);
            body.addProperty("think",false);

            bodyString = body.toString();
            log.debug("Ollama API call:\n{}", bodyString);

            Response response = callApi(conn, bodyString, client);

            JsonObject root = response.responseData();

            // Ollama gives clean JSON — no \n\n{ garbage!
            JsonObject message = root.getAsJsonObject("message");
            if (message == null || !message.has("content")) {
                log.error("No message/content from Ollama:\n{}", root);
                return null;
            }

            String content = message.get("content").getAsString();
            log.debug("Ollama raw response:\n{}", content);

            // Ollama returns pure JSON string — just parse it
            return JsonParser.parseString(content).getAsJsonObject();

        } catch (Exception e) {
            log.error("Ollama chat call failed: {}", e.getMessage(), e);
            log.error("Request body was:\n{}", bodyString != null ? bodyString : "null");
            return null;
        }
    }
}
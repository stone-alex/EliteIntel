// elite/intel/ai/brain/ollama/OllamaAnalysisEndpoint.java
package elite.intel.ai.brain.ollama;

import com.google.gson.*;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiAnalysisInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OllamaAnalysisEndpoint extends AiEndPoint implements AiAnalysisInterface {

    private static final Logger log = LogManager.getLogger(OllamaAnalysisEndpoint.class);
    private static final OllamaAnalysisEndpoint INSTANCE = new OllamaAnalysisEndpoint();
    private final Gson gson = GsonFactory.getGson();

    private OllamaAnalysisEndpoint() {}

    public static OllamaAnalysisEndpoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject analyzeData(String userIntent, AiData struct) {
        try {
            OllamaClient client = OllamaClient.getInstance();
            var conn = client.getHttpURLConnection();

            String systemPrompt = ApiFactory.getInstance()
                    .getAiPromptFactory()
                    .generateAnalysisPrompt(userIntent, struct.getInstructions());

            JsonObject body = client.createRequestBodyHeader(OllamaClient.MODEL_TINYLAMA, 0.3f);

            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", AIConstants.ROLE_SYSTEM);
            systemMsg.addProperty("content", systemPrompt);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", AIConstants.ROLE_USER);
            userMsg.addProperty("content", "User intent: " + userIntent + "\nData: " + struct.getData().toJson());

            JsonArray messages = new JsonArray();
            messages.add(systemMsg);
            messages.add(userMsg);
            body.add("messages", messages);

            log.debug("Ollama analysis call:\n{}", gson.toJson(body));

            Response response = callApi(conn, gson.toJson(body), client);
            JsonObject root = response.responseData();

            String content = root.getAsJsonObject("message")
                    .get("content")
                    .getAsString();

            log.debug("Ollama analysis raw response:\n{}", content);

            return JsonParser.parseString(content).getAsJsonObject();

        } catch (Exception e) {
            log.error("Ollama analysis failed", e);
            JsonObject err = new JsonObject();
            err.addProperty("response_text", "Analysis failed – check logs");
            return err;
        }
    }
}
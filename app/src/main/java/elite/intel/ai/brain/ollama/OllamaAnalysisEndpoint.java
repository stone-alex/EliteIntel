// elite/intel/ai/brain/ollama/OllamaAnalysisEndpoint.java
package elite.intel.ai.brain.ollama;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    private OllamaAnalysisEndpoint() {
    }

    public static OllamaAnalysisEndpoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject analyzeData(String originalUserInput, AiData struct) {
        try {
            OllamaClient client = OllamaClient.getInstance();
            var conn = client.getHttpURLConnection();

            String systemPrompt = ApiFactory.getInstance()
                    .getAiPromptFactory()
                    .generateAnalysisPrompt();

            JsonObject prompt = client.createPrompt(OllamaClient.MODEL_OLLAMA, 1.0f);

            JsonObject systemMsg1 = new JsonObject();
            systemMsg1.addProperty("role", AIConstants.ROLE_SYSTEM);
            systemMsg1.addProperty("content", systemPrompt);

            JsonObject systemMsg2 = new JsonObject();
            systemMsg2.addProperty("role", AIConstants.ROLE_SYSTEM);
            systemMsg2.addProperty("content", "ADDITIONAL QUERY-SPECIFIC INSTRUCTIONS: User Asks question: \"" + originalUserInput +"\" Instructions for the data: "+ struct.getInstructions());

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", AIConstants.ROLE_USER);
            userMsg.addProperty("content", struct.getData().toJson());

            JsonArray messages = new JsonArray();
            messages.add(systemMsg1);
            messages.add(systemMsg2);
            messages.add(userMsg);
            prompt.add("messages", messages);

            log.debug("Ollama analysis call:\n{}", gson.toJson(prompt));

            Response response = processAiPrompt(conn, gson.toJson(prompt), client);
            JsonObject root = response.responseData();

            log.debug("Ollama analysis raw response:\n{}", gson.toJson(root));
            return JsonParser.parseString(root.getAsJsonObject("message").get("content").getAsString()).getAsJsonObject();

        } catch (Exception e) {
            log.error("Ollama analysis failed", e);
            JsonObject err = new JsonObject();
            err.addProperty("response_text", "Analysis failed – check logs");
            return err;
        }
    }
}
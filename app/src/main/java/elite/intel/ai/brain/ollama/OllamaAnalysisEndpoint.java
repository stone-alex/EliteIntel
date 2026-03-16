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
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OllamaAnalysisEndpoint extends AiEndPoint implements AiAnalysisInterface {

    private static final Logger log = LogManager.getLogger(OllamaAnalysisEndpoint.class);
    private static final OllamaAnalysisEndpoint INSTANCE = new OllamaAnalysisEndpoint();
    private final Gson gson = GsonFactory.getGson();
    private final ApiFactory apiFactory = ApiFactory.getInstance();

    private OllamaAnalysisEndpoint() {
    }

    public static OllamaAnalysisEndpoint getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject analyzeData(String originalUserInput, AiData struct) {
        try {
            OllamaClient client = OllamaClient.getInstance();

            JsonObject prompt = client.createPrompt(OllamaClient.MODEL_QUERIES, 0.70f);

            JsonObject systemMsg1 = new JsonObject();
            systemMsg1.addProperty("role", AIConstants.ROLE_SYSTEM);
            systemMsg1.addProperty("content", apiFactory.getAiPromptFactory().generateAnalysisPrompt());

            JsonObject systemMsg2 = new JsonObject();
            systemMsg2.addProperty("role", AIConstants.ROLE_SYSTEM);
            systemMsg2.addProperty("content", "INSTRUCTIONS: " + struct.getInstructions());

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", AIConstants.ROLE_USER);
            userMsg.addProperty("content", "User say: [" + originalUserInput + "] data: [" + struct.getData().toYaml() + "]");

            JsonArray messages = new JsonArray();
            messages.add(systemMsg1);
            messages.add(systemMsg2);
            messages.add(userMsg);
            prompt.add("messages", messages);

            log.debug("Ollama analysis call:\n{}", gson.toJson(prompt));

            JsonObject properties = new JsonObject();
            JsonObject typeProp = new JsonObject();
            typeProp.addProperty("type", "string");
            properties.add("type", typeProp);
            JsonObject responseTextProp = new JsonObject();
            responseTextProp.addProperty("type", "string");
            properties.add("response_text", responseTextProp);

            JsonObject format = new JsonObject();
            format.add("properties", properties);
            JsonArray required = new JsonArray();
            required.add("type");
            required.add("response_text");
            format.add("required", required);
            format.addProperty("additionalProperties", false);
            format.addProperty("type", "object");
            prompt.add("format", format);

            JsonObject root = processAiPrompt(gson.toJson(prompt), client);
            log.debug("Ollama analysis raw response:\n{}", gson.toJson(root));
            String content = root.getAsJsonObject("message").get("content").getAsString();
            JsonObject parsed = JsonParser.parseString(JsonUtils.repairLlmJson(content)).getAsJsonObject();
            parsed.addProperty("type", "chat"); // normalize - model sometimes hallucinates type values
            return parsed;

        } catch (Exception e) {
            log.error("Ollama analysis failed", e);
            JsonObject err = new JsonObject();
            err.addProperty("response_text", "Analysis failed – check logs");
            return err;
        }
    }

    public JsonObject processSensor(SensorDataEvent event) {
        try {
            OllamaClient client = OllamaClient.getInstance();
            JsonObject prompt = client.createPrompt(OllamaClient.MODEL_QUERIES, 0.70f);

            // Build messages array
            JsonArray messages = new JsonArray();

            JsonObject sensorPrompt = new JsonObject();
            sensorPrompt.addProperty("role", AIConstants.ROLE_SYSTEM);
            sensorPrompt.addProperty("content", ApiFactory.getInstance().getAiPromptFactory().generateSensorPrompt());
            messages.add(sensorPrompt);

            JsonObject instructions = new JsonObject();
            instructions.addProperty("role", AIConstants.ROLE_SYSTEM);
            instructions.addProperty("content", "EVENT SPECIFIC INSTRUCTIONS: " + event.getInstructions());
            messages.add(instructions);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", AIConstants.ROLE_USER);
            userMsg.addProperty("content", event.getSensorData());
            messages.add(userMsg);

            prompt.add("messages", messages);

            // Chat schema - sensor responses are type + response_text only
            JsonObject properties = new JsonObject();
            JsonObject typeProp = new JsonObject();
            typeProp.addProperty("type", "string");
            properties.add("type", typeProp);
            JsonObject responseTextProp = new JsonObject();
            responseTextProp.addProperty("type", "string");
            properties.add("response_text", responseTextProp);

            JsonObject format = new JsonObject();
            format.add("properties", properties);
            JsonArray required = new JsonArray();
            required.add("type");
            required.add("response_text");
            format.add("required", required);
            format.addProperty("additionalProperties", false);
            format.addProperty("type", "object");
            prompt.add("format", format);

            JsonObject root = processAiPrompt(gson.toJson(prompt), client);
            log.debug("Ollama sensor raw response:\n{}", gson.toJson(root));
            String sensorContent = root.getAsJsonObject("message").get("content").getAsString();
            JsonObject parsed = JsonParser.parseString(JsonUtils.repairLlmJson(sensorContent)).getAsJsonObject();
            parsed.addProperty("type", "chat");
            return parsed;

        } catch (Exception e) {
            log.error("Ollama sensor processing failed", e);
            JsonObject err = new JsonObject();
            err.addProperty("type", "chat");
            err.addProperty("response_text", "Sensor analysis failed – check logs");
            return err;
        }
    }
}
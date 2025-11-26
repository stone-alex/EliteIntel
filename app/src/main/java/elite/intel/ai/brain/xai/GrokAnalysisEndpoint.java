package elite.intel.ai.brain.xai;

import com.google.gson.*;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiAnalysisInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

import static elite.intel.ai.brain.AIConstants.PROPERTY_CONTENT;
import static elite.intel.ai.brain.AIConstants.PROPERTY_MESSAGE;

public class GrokAnalysisEndpoint extends AiEndPoint implements AiAnalysisInterface {
    private static final Logger logger = LogManager.getLogger(GrokAnalysisEndpoint.class);
    private final Gson gson = GsonFactory.getGson();
    private static final GrokAnalysisEndpoint instance = new GrokAnalysisEndpoint();
    ApiFactory apiFactory = ApiFactory.getInstance();

    private GrokAnalysisEndpoint() {
    }

    public static GrokAnalysisEndpoint getInstance() {
        return instance;
    }

    @Override public JsonObject analyzeData(String userIntent, AiData struct) {
        try {
            GrokClient client = GrokClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();

            String systemPrompt = apiFactory.getAiPromptFactory().generateAnalysisPrompt(userIntent, struct.getInstructions());

            JsonObject request = client.createRequestBodyHeader(GrokClient.MODEL_GROK_4_FAST_REASONING, 1);

            JsonObject messageSystem = new JsonObject();
            messageSystem.addProperty("role", AIConstants.ROLE_SYSTEM);
            messageSystem.addProperty("content", systemPrompt);

            JsonObject messageUser = new JsonObject();
            messageUser.addProperty("role", AIConstants.ROLE_USER);
            messageUser.addProperty("content", "User intent: " + userIntent + "\nData: " + struct.getData().toJson());

            request.add("messages", gson.toJsonTree(new Object[]{messageSystem, messageUser}));

            String jsonString = gson.toJson(request);
            logger.debug("xAI API call:\n{}", jsonString);

            Response response = callApi(conn, jsonString, client);

            JsonArray choices = response.responseData().getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                logger.error("No choices in API response:\n{}", response);
                return client.createErrorResponse("Analysis error.");
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                logger.error("No message in API response choices:\n{}", response.responseMessage());
                return client.createErrorResponse("Analysis error.");
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                logger.error("No content in API response message:\n{}", response.responseMessage());
                return client.createErrorResponse("Analysis error.");
            }

            logger.debug("API response content:\n{}", content);

            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                jsonStart = content.indexOf("{");
                if (jsonStart == -1) {
                    logger.error("No JSON object found in content:\n{}", content);
                    return client.createErrorResponse("Analysis error.");
                }
                jsonContent = content.substring(jsonStart);
            }

            logger.info("Extracted JSON content:\n\n{}\n\n", jsonContent);

            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.error("Failed to parse API response content:\n{}", jsonContent, e);
                return client.createErrorResponse("Analysis error.");
            }
        } catch (Exception e) {
            logger.error("AI API call fatal error: {}", e.getMessage(), e);
            return GrokClient.getInstance().createErrorResponse("Analysis error. Check logs.");
        }
    }
}
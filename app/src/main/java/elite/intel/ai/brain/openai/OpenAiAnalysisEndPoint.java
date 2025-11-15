package elite.intel.ai.brain.openai;

import com.google.gson.*;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.AiAnalysisInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;

import static elite.intel.ai.brain.AIConstants.PROPERTY_CONTENT;
import static elite.intel.ai.brain.AIConstants.PROPERTY_MESSAGE;

public class OpenAiAnalysisEndPoint extends AiEndPoint implements AiAnalysisInterface {
    private static final Logger log = LogManager.getLogger(OpenAiAnalysisEndPoint.class);
    private static OpenAiAnalysisEndPoint instance;

    private OpenAiAnalysisEndPoint() {
    }

    public static synchronized OpenAiAnalysisEndPoint getInstance() {
        if (instance == null) {
            instance = new OpenAiAnalysisEndPoint();
        }
        return instance;
    }

    @Override public JsonObject analyzeData(String originalUserInput, AiData struct) {
        try {
            OpenAiClient client = OpenAiClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();
            String systemPrompt = ApiFactory.getInstance().getAiPromptFactory().generateAnalysisPrompt(originalUserInput, struct.getInstructions());

            JsonObject request = client.createRequestBodyHeader(OpenAiClient.MODEL_GPT_4_1_MINI, 1);

            JsonObject messageSystem = new JsonObject();
            messageSystem.addProperty("role", AIConstants.ROLE_SYSTEM);
            messageSystem.addProperty("content", systemPrompt);

            JsonObject messageUser = new JsonObject();
            messageUser.addProperty("role", AIConstants.ROLE_USER);
            messageUser.addProperty("content", "User Input: " + originalUserInput + ". Data: " + struct.getData().toJson()+". ");

            JsonArray messages = new JsonArray();
            messages.add(messageSystem);
            messages.add(messageUser);
            request.add("messages", messages);

            String jsonString = GsonFactory.getGson().toJson(request);
            log.debug("Analysis call:\n\n{}\n\n", jsonString);

            Response response = callApi(conn, jsonString, client);

            JsonElement jsonElement = response.responseData().get("usage");
            if(jsonElement != null) {
                log.info("API usage:\n{}", ("Prompt Tokens: "+jsonElement.getAsJsonObject().get("prompt_tokens")+"  Total Tokens:"+ jsonElement.getAsJsonObject().get("total_tokens")));
            }

            JsonArray choices = response.responseData().getAsJsonArray("choices");

            if (choices == null || choices.isEmpty()) {
                log.error("No choices in API response:\n{}", response.responseMessage());
                return client.createErrorResponse("Analysis error.");
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject(PROPERTY_MESSAGE);
            if (message == null) {
                log.error("No message in API response choices:\n{}", response.responseMessage());
                return client.createErrorResponse("Analysis error.");
            }

            log.debug("AI Response: \n\n{}\n\n", GsonFactory.getGson().toJson(message));

            String content = message.get(PROPERTY_CONTENT).getAsString();
            if (content == null) {
                log.error("No content in API response message:\n{}", response.responseMessage());
                return client.createErrorResponse("Analysis error.");
            }

            log.debug("API response content:\n{}", content);

            // Extract JSON from content
            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                jsonStart = content.indexOf("{");
                if (jsonStart == -1) {
                    log.error("No JSON object found in content:\n{}", content);
                    return client.createErrorResponse("Analysis error.");
                }
                jsonContent = content.substring(jsonStart);
            }

            // Parse extracted JSON content
            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content:\n{}", jsonContent, e);
                return client.createErrorResponse("Analysis error.");
            }
        } catch (IOException e) {
            log.error("Open AI API call failed: {}", e.getMessage(), e);
            return OpenAiClient.getInstance().createErrorResponse("Analysis error. Check logs.");
        } catch (Exception e) {
            log.error("AI API call fatal error: {}", e.getMessage(), e);
            return OpenAiClient.getInstance().createErrorResponse("Analysis error. Check logs.");
        }
    }
}
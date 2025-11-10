package elite.intel.ai.brain.xai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.AiQueryInterface;
import elite.intel.ai.brain.commons.AiEndPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class GrokQueryEndPoint extends AiEndPoint implements AiQueryInterface {
    private static final Logger log = LogManager.getLogger(GrokQueryEndPoint.class);
    private static final GrokQueryEndPoint INSTANCE = new GrokQueryEndPoint();

    private GrokQueryEndPoint() {
        // Private constructor for singleton
    }

    public static GrokQueryEndPoint getInstance() {
        return INSTANCE;
    }

    @Override public JsonObject sendToAi(JsonArray messages) {
        String bodyString = null;
        try {
            JsonArray sanitizedMessages = sanitizeJsonArray(messages);

            GrokClient client = GrokClient.getInstance();
            HttpURLConnection conn = client.getHttpURLConnection();

            JsonObject body = client.createRequestBodyHeader(GrokClient.MODEL_GROK_4_FAST_REASONING, 1);
            body.add("messages", sanitizedMessages);

            bodyString = body.toString();
            log.debug("xAI API query call:\n{}", bodyString);

            Response response = callApi(conn, bodyString, client);
            StracturedResponse stracturedResponse = checkResponse(response);
            if (!stracturedResponse.isSuccessful()) {
                return null;
            }

            log.debug("API response content:\n{}", stracturedResponse.content());

            String jsonContent;
            int jsonStart = stracturedResponse.content().indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = stracturedResponse.content().substring(jsonStart + 2); // Skip \n\n
            } else {
                jsonStart = stracturedResponse.content().indexOf("{");
                if (jsonStart == -1) {
                    log.error("No JSON object found in content:\n{}", stracturedResponse.content());
                    return null;
                }
                jsonContent = stracturedResponse.content().substring(jsonStart);
                try {
                    JsonParser.parseString(jsonContent);
                } catch (JsonSyntaxException e) {
                    log.error("Invalid JSON object in content:\n{}", jsonContent, e);
                    return null;
                }
            }

            log.debug("Extracted JSON content:\n\n{}\n\n", jsonContent);

            try {
                return JsonParser.parseString(jsonContent).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse API response content:\n{}", jsonContent, e);
                throw e;
            }
        } catch (Exception e) {
            log.error("AI API query call fatal error: {}", e.getMessage(), e);
            log.error("Input data:\n{}", bodyString != null ? bodyString : "null");
            return null;
        }
    }
}
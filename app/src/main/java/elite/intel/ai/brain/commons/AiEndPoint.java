package elite.intel.ai.brain.commons;

import com.google.gson.*;
import elite.intel.ai.brain.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public abstract class AiEndPoint {

    private static final Logger log = LogManager.getLogger(AiEndPoint.class);

    protected JsonArray sanitizeJsonArray(JsonArray messages) {
        if (messages == null) {
            return new JsonArray();
        }
        JsonArray sanitized = new JsonArray();
        for (int i = 0; i < messages.size(); i++) {
            JsonObject original = messages.get(i).getAsJsonObject();
            JsonObject sanitizedObj = new JsonObject();
            sanitizedObj.addProperty("role", original.get("role").getAsString());
            JsonElement content = original.get("content");
            if (content != null) {
                sanitizedObj.addProperty("content", content.getAsString());
            }
            sanitized.add(sanitizedObj);
        }
        return sanitized;
    }


    public JsonObject processAiPrompt(String jsonString, Client client) throws IOException {
        log.debug("xAI API call:\n{}", jsonString);
        return client.sendJsonRequest(jsonString);
    }

    public StructuredResponse checkResponse(JsonObject response) {
        JsonArray choices = response.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("No choices in API response:\n{}", response);
            return new StructuredResponse(null, null, null, false);
        }

        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null) {
            log.error("No message in API response choices:\n{}", response);
            return new StructuredResponse(null, null, null, false);
        }

        String content = message.get("content").getAsString();
        if (content == null) {
            log.error("No content in API response message:\n{}", response);
            return new StructuredResponse(null, null, null, false);
        }
        return new StructuredResponse(choices, message, content, true);
    }

    public record StructuredResponse(JsonArray choices, JsonObject message, String content, boolean isSuccessful) {
    }

}

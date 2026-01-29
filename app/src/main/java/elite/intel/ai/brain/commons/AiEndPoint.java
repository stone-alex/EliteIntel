package elite.intel.ai.brain.commons;

import com.google.gson.*;
import elite.intel.ai.brain.Client;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.LlmMetadata;
import elite.intel.util.json.OllamaMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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


    public JsonObject processServerError(HttpURLConnection conn, int responseCode, Client client) throws IOException {
        String errorResponse = "";
        try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
            errorResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            log.warn("Failed to read error stream: {}", e.getMessage());
        }
        log.error("AI API error: {} - {}", responseCode, conn.getResponseMessage() + " " + errorResponse);
        return client.createErrorResponse("API error: " + responseCode);
    }


    public JsonObject processAiPrompt(String jsonString, Client client) throws IOException {
        log.debug("xAI API call:\n{}", jsonString);
        return client.sendJsonRequest(jsonString);
    }

    public StracturedResponse checkResponse(JsonObject response) {
        JsonArray choices = response.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("No choices in API response:\n{}", response);
            return new StracturedResponse(null, null, null, false);
        }

        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null) {
            log.error("No message in API response choices:\n{}", response);
            return new StracturedResponse(null, null, null, false);
        }

        String content = message.get("content").getAsString();
        if (content == null) {
            log.error("No content in API response message:\n{}", response);
            return new StracturedResponse(null, null, null, false);
        }
        return new StracturedResponse(choices, message, content, true);
    }

    public record Response(JsonObject responseData, String responseMessage) {
    }

    public record StracturedResponse(JsonArray choices, JsonObject message, String content, boolean isSuccessful) {
    }

}

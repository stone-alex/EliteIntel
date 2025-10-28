package elite.intel.ai.brain.commons;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import elite.intel.ai.brain.Client;
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
            sanitizedObj.addProperty("content", escapeJson(original.get("content").getAsString()));
            sanitized.add(sanitizedObj);
        }
        return sanitized;
    }

    protected String escapeJson(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c < 32 || c == 127 || c == 0xFEFF) {
                sb.append(' '); // Replace control characters
            } else if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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


    public Response callApi(HttpURLConnection conn, String jsonString, Client client) throws IOException {
        try (var outputStream = conn.getOutputStream()) {
            outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        String response;
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
            response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }

        if (response.startsWith("\uFEFF")) {
            response = response.substring(1);
            log.info("Stripped BOM from response");
        }

        log.debug("Open AI API response:\n{}", response);

        if (responseCode != 200) {
            return new Response(processServerError(conn, responseCode, client), response);
        }


        try {
            return new Response(JsonParser.parseString(response).getAsJsonObject(), response);
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse API response:\n{}", response, e);
            return new Response(client.createErrorResponse("Failed to parse API response"), response);
        }
    }

    public record Response(JsonObject responseData, String responseMessage) {
    }


    public StracturedResponse checkResponse(Response response) {
        JsonArray choices = response.responseData().getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("No choices in API response:\n{}", response.responseMessage());
            return new StracturedResponse(null,null,null,false);
        }

        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null) {
            log.error("No message in API response choices:\n{}", response.responseMessage());
            return new StracturedResponse(null,null,null,false);
        }

        String content = message.get("content").getAsString();
        if (content == null) {
            log.error("No content in API response message:\n{}", response.responseMessage());
            return new StracturedResponse(null,null,null,false);
        }
        return new StracturedResponse(choices,message,content, true);
    }

    public record StracturedResponse(JsonArray choices, JsonObject message, String content, boolean isSuccessful) {
    }

}

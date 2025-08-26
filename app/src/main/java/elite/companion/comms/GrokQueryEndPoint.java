package elite.companion.comms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.Globals;
import elite.companion.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GrokQueryEndPoint {
    private static final Logger log = LoggerFactory.getLogger(GrokQueryEndPoint.class);
    private static final GrokQueryEndPoint INSTANCE = new GrokQueryEndPoint();

    private GrokQueryEndPoint() {
        // Private constructor for singleton
    }

    public static GrokQueryEndPoint getInstance() {
        return INSTANCE;
    }

    /**
     * Sends a full message history to Grok for query follow-up.
     * The first message should be the system prompt (role: system).
     * Returns the parsed JSON response content or null on error.
     */
    public JsonObject sendToGrok(JsonArray messages) {
        try {
            HttpURLConnection conn = getHttpURLConnection();

            JsonObject body = new JsonObject();
            body.addProperty("model", "grok-3-fast");
            body.addProperty("temperature", 0.7);
            body.addProperty("stream", false);
            body.add("messages", messages);

            String bodyString = body.toString();
            log.info("xAI API query call: {}", bodyString);

            try (var os = conn.getOutputStream()) {
                os.write(bodyString.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    String response = scanner.useDelimiter("\\A").next();
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    String content = json.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .get("message").getAsJsonObject()
                            .get("content").getAsString();
                    return JsonParser.parseString(content).getAsJsonObject();
                }
            } else {
                String errorResponse = "";
                try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                    if (scanner.hasNext()) {
                        errorResponse = scanner.useDelimiter("\\A").next();
                    }
                } catch (Exception e) {
                    log.warn("Failed to read error stream: {}", e.getMessage());
                }
                log.error("xAI API error: {} - {}", responseCode, conn.getResponseMessage());
                log.info("Error response body: {}", errorResponse);
                return null;
            }
        } catch (Exception e) {
            log.error("AI API query call fatal error: {}", e.getMessage(), e);
            return null;
        }
    }

    private static HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL("https://api.x.ai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ConfigManager.getInstance().readConfig(Globals.XAI_API_KEY).get("key"));
        conn.setDoOutput(true);
        return conn;
    }
}
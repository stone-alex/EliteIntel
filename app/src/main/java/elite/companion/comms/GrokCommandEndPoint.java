package elite.companion.comms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.Globals;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.AIContextFactory;
import elite.companion.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GrokCommandEndPoint {
    private static final Logger log = LoggerFactory.getLogger(GrokCommandEndPoint.class);

    private static final ThreadLocal<JsonArray> currentHistory = new ThreadLocal<>();

    public void start() throws Exception {
        GrokResponseRouter.getInstance().start();
        log.info("Started GrokInteractionHandler");
    }

    public void stop() {
        GrokResponseRouter.getInstance().stop();
        log.info("Stopped GrokInteractionHandler");
    }

    public void processVoiceCommand(String command) {
        if (command == null || command.isEmpty()) {
            VoiceGenerator.getInstance().speak("Sorry, I couldn't process that.");
            return;
        }

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", AIContextFactory.getInstance().generateSystemPrompt());
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", buildVoiceRequest(command));
        messages.add(userMessage);

        System.out.println("Voice command messages: " + messages);
        JsonObject apiResponse = callXaiApi(messages); // Pass messages directly
        if (apiResponse == null) {
            VoiceGenerator.getInstance().speak("Sorry, I couldn't process that.");
            return;
        }

        GrokResponseRouter.getInstance().processGrokResponse(apiResponse);
    }

    public void processSystemCommand() {
        String sensor_data = SystemSession.getInstance().getSensorData();
        String fssData = SystemSession.getInstance().getFssData();
        String input = (sensor_data == null && fssData == null) ? null : (sensor_data == null ? "" : sensor_data) + (fssData == null ? "" : fssData);

        if (sensor_data != null) SystemSession.getInstance().clearSensorData();
        if (fssData != null) SystemSession.getInstance().clearFssData();

        if (input == null || input.isEmpty()) {
            return;
        }

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", AIContextFactory.getInstance().generateSystemPrompt());
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", buildSystemRequest(input));
        messages.add(userMessage);

        System.out.println("System command messages: " + messages);
        JsonObject apiResponse = callXaiApi(messages);
        if (apiResponse == null) {
            VoiceGenerator.getInstance().speak("Failure processing system request. Check programming");
            return;
        }

        GrokResponseRouter.getInstance().processGrokResponse(apiResponse);
    }

    public static JsonArray getCurrentHistory() {
        return currentHistory.get() != null ? currentHistory.get() : new JsonArray();
    }

    /**
     * Reacts to user voice input and generates a JSON request for the xAI API.
     * Can and will trigger game controls.
     *
     */
    private String buildVoiceRequest(String transcribedText) {
        return AIContextFactory.getInstance().generatePlayerInstructions(
                String.valueOf(transcribedText), PlayerSession.getInstance().getSummary()
        );
    }

    /**
     * Reacts to system sensor input and generates a JSON request for the xAI API.
     * Used for reactions, does not trigger game controls.
     *
     */
    private String buildSystemRequest(String systemInput) {
        return AIContextFactory.getInstance().generateSystemInstructions(systemInput);
    }

    private JsonObject callXaiApi(JsonArray messages) {
        try {
            HttpURLConnection conn = getHttpURLConnection();

            JsonObject body = new JsonObject();
            body.addProperty("model", "grok-3-fast");
            body.addProperty("temperature", 0.7);
            body.addProperty("stream", false);
            body.add("messages", messages);

            String bodyString = body.toString();
            log.info("xAI API call: {}", bodyString);
            currentHistory.set(messages); // Store before call

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
            log.error("AI API call fatal error: {}", e.getMessage(), e);
            log.error("Input data {} ", messages);

            return null;
        } finally {
            currentHistory.remove(); // Always clear
        }
    }

    private static HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL("https://api.x.ai/v1/chat/completions"); //TODO: Read from config or settings.
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ConfigManager.getInstance().readConfig(Globals.XAI_API_KEY).get("key"));
        conn.setDoOutput(true);
        return conn;
    }
}
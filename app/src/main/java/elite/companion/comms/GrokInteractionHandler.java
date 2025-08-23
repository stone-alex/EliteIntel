package elite.companion.comms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.Globals;
import elite.companion.session.SessionTracker;
import elite.companion.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GrokInteractionHandler {

    private static final Logger log = LoggerFactory.getLogger(GrokInteractionHandler.class);


    public void processVoiceCommand(String voiceCommand) {
        String command = sanitizeGoogleMistakes(voiceCommand);
        if (command == null) {return;}

        String request = buildRequest(command);
        String apiResponse = callXaiApi(request);
        if (apiResponse.isEmpty()) {
            VoiceGenerator.getInstance().speak("Sorry, I couldn't process that.");
            return;
        }
        GrokCommandProcessor.getInstance().processResponse(apiResponse);
    }

    private static String sanitizeGoogleMistakes(String voiceCommand) {
        if (voiceCommand == null || voiceCommand.isEmpty()) return null;

        String command = voiceCommand.toLowerCase().trim();

        // Map misheard phrases to "tritium"
        String[] misheardPhrases = {
                "treat you", "trees you", "3 tube", "hydrogen 3", "hydrogen three",
                "carrier fuel", "carrier juice", "carrot juice", "treatyou", "treesyou"
        };
        for (String phrase : misheardPhrases) {
            if (command.contains(phrase)) {
                command = command.replaceAll("(?i)" + phrase.replace(" ", "\\s+"), "tritium");
                log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
            }
        }
        return command;
    }


    private String buildRequest(String transcribedText) {
        String stateSummary = SessionTracker.getInstance().getStateSummary();
        return String.format(
                "Interpret this user voice input: '%s'. " +
                        "Current game state: %s. " +
                        "Classify as: 'command' (trigger app action or keyboard event), 'query' (request info from state), or 'chat' (general or unclear talk). " +
                        "If unclear or noise (e.g., sniff or gibberish), classify as 'chat' and respond lightly like 'Didn't catch that!'. %s %s " +
                        "Respond in JSON only: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output (concise and fun)\", \"action\": \"set_mining_target|open_cargo_hatch|...\" (if command), \"params\": {\"key\": \"value\"} (if command)}. " +
                        "Use provided state for queries; say 'I don't know' if data unavailable. " +
                        "Never automate—actions must be user-triggered.",
                transcribedText, stateSummary, GrokRequestHints.supportedCommands, GrokRequestHints.supportedQueries, GrokRequestHints.supportedConcepts
        );
    }

    private String callXaiApi(String prompt) {
        try {
            HttpURLConnection conn = getHttpURLConnection();

            // Build JSON body programmatically
            JsonObject body = new JsonObject();
            body.addProperty("model", "grok-4-latest");
            body.addProperty("temperature", 0.7);
            body.addProperty("stream", false);

            JsonArray messages = new JsonArray();
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "You are an AI assistant for an Elite Dangerous companion app, processing voice commands.");
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt); // Gson handles escaping
            messages.add(userMessage);

            body.add("messages", messages);

            String bodyString = body.toString();
            log.info("xAI API call: {}", bodyString);

            try (var os = conn.getOutputStream()) {
                os.write(bodyString.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    String response = scanner.useDelimiter("\\A").next();
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    return json.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .get("message").getAsJsonObject()
                            .get("content").getAsString();
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
                return "";
            }
        } catch (Exception e) {
            log.error("AI API call fatal error: {}", e.getMessage(), e);
            return "";
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
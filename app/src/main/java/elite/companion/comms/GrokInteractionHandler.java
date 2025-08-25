package elite.companion.comms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.Globals;
import elite.companion.session.PublicSession;
import elite.companion.session.SystemSession;
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

    public void start() throws Exception {
        GrokResponseRouter.getInstance().start();
        log.info("Started GrokInteractionHandler");
    }

    public void stop() {
        GrokResponseRouter.getInstance().stop();
        log.info("Stopped GrokInteractionHandler");
    }

    public void processVoiceCommand(String voiceCommand) {
        String command = sanitizeGoogleMistakes(voiceCommand);
        if (command == null) {
            VoiceGenerator.getInstance().speak("Sorry, I couldn't process that.");
            return;
        }

        String request = buildVoiceRequest(command);
        System.out.println( request);
        JsonObject apiResponse = callXaiApi(request);
        if (apiResponse == null) {
            VoiceGenerator.getInstance().speak("Sorry, I couldn't process that.");
            return;
        }

        GrokResponseRouter.getInstance().processGrokResponse(apiResponse);
    }

    public void processSystemCommand() {
        String input = SystemSession.getInstance().getSensorData();
        SystemSession.getInstance().clearSensorData();
        if (input == null || input.isEmpty()) {return;}

        String request = buildSystemRequest(input);
        System.out.println( request);
        JsonObject apiResponse = callXaiApi(request);
        if (apiResponse == null) {
            VoiceGenerator.getInstance().speak("Failure processing system request. Check programming");
            return;
        }

        GrokResponseRouter.getInstance().processGrokResponse(apiResponse);
    }


    private static String sanitizeGoogleMistakes(String voiceCommand) {
        if (voiceCommand == null || voiceCommand.isEmpty()) return null;

        String command = voiceCommand.toLowerCase().trim();

        String[] misheardPhrases = {
                "treat you", "trees you", "3 tube", "hydrogen 3", "hydrogen three", "32",
                "carrier fuel", "carrier juice", "carrot juice", "treatyou", "treesyou",
        };
        for (String phrase : misheardPhrases) {
            if (command.contains(phrase)) {
                command = command.replaceAll("(?i)" + phrase.replace(" ", "\\s+"), "tritium");
                log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
            }
        }
        return command;
    }

    /**
     * Reacts to user voice input and generates a JSON request for the xAI API.
     * Can and will trigger game controls.
     * */
    private String buildVoiceRequest(String transcribedText) {
        String stateSummary = PublicSession.getInstance().getStateSummary();
        return String.format(
                "Interpret this user voice input: "+transcribedText+". " +
                        "Current game state: "+stateSummary+". " +
                        "Classify as: 'command' (trigger app action or keyboard event), 'query' (request info from state), or 'chat' (general or unclear talk). " +
                        GrokRequestHints.supportedCommands +
                        "If unclear or noise (e.g., sniff or gibberish), classify as 'chat' and respond lightly like 'Didn't catch that!'. " +
                        "Respond in JSON only: {\"type\": \"command|query|chat\", \"response_text\": \"TTS output (concise and fun)\", \"action\": \"set_mining_target|open_cargo_hatch|...\" (if command or query), \"params\": {\"key\": \"value\"} (if command or query)}. " +
                        "Use provided state for queries; say 'I don't know' if data unavailable. " +
                        "Never automate—actions must be user-triggered."
        );
    }

    /**
     * Reacts to system sensor input and generates a JSON request for the xAI API.
     * Used for reactions, does not trigger game controls.
     * */
    private String buildSystemRequest(String systemInput) {
        String stateSummary = PublicSession.getInstance().getStateSummary();
        SystemSession.getInstance().clear();
        return String.format(
                "Analyze this ship sensor input : "+systemInput+". provide extremely brief but fun response and optional system_command " +

                        "If radio_transmission:[data] provide only extremely brief response_text (chat) do not include the transmission in the response (user already knows what it is). " +
                        "If is_station=true message was for us. else it is radio chatter." +

                        "Context: Current game state: "+stateSummary+". " +
                        "Classify as: 'system_command' (sets companion app variables, provides fun response TTS output, but does not trigger game controls), or 'chat' (general banter)." +
                        "Respond in JSON only: {\"type\": \"system_command|chat\", \"response_text\": \"TTS output (extremely brief but fun)\", \"action\": \"set_mining_target|set_current_system|...\" , \"params\": {\"key\": \"value\"}}."
        );
    }

    private JsonObject callXaiApi(String prompt) {
        try {
            HttpURLConnection conn = getHttpURLConnection();

            JsonObject body = new JsonObject();
            //body.addProperty("model", "grok-4-latest");
            body.addProperty("model", "grok-3-fast");
            body.addProperty("temperature", 0.7);
            body.addProperty("stream", false);

            JsonArray messages = new JsonArray();
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "You are an AI assistant for an Elite Dangerous companion app, processing voice commands.");
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
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
package elite.companion.comms.ai;

import com.google.gson.*;
import elite.companion.session.SystemSession;
import elite.companion.util.ConfigManager;
import elite.companion.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GrokAnalysisEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(GrokAnalysisEndpoint.class);
    public static final String ERROR_REPONSE = "{\"response_text\": \"Analysis error, Check logs.\"}";
    private final String apiUrl = "https://api.x.ai/v1/chat/completions";
    private final Gson gson = GsonFactory.getGson();
    private static final GrokAnalysisEndpoint instance = new GrokAnalysisEndpoint();

    private GrokAnalysisEndpoint() {
    }

    public static GrokAnalysisEndpoint getInstance() {
        return instance;
    }

    public String analyzeData(String userIntent, String dataJson) {
        try {
            HttpURLConnection conn = getHttpURLConnection();
            String aiName = SystemSession.getInstance().getAIVoice().getName();
            String systemPrompt = " Context: You are " + aiName + ", onboard AI for Elite Dangerous. Address as My Lord. Brief, concise, military professional. British cadence, NATO phonetic alphabet for codes (e.g., RH-F = Romeo Hotel dash Foxtrot), spell out numerals (e.g., 285 = two eight five).\n" +
                    "   Task: Analyze provided JSON data against user intent. Return precise answers (e.g., yes/no for specific searches) or summaries as requested. Output JSON: {\"response_text\": \"TTS output\", \"details\": \"optional extra info\"}\n" +
                    "   Data format: JSON array of signals, e.g., [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}, {\"name\": \"Distress Signal\", \"type\": \"USS\"}]\n" +
                    "   Examples:\n" +
                    "       - Intent: 'tell me if carrier XYZ is here' Data: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}] -> {\"response_text\": \"Fleet carrier XYZ is present, My Lord.\", \"details\": \"Detected in local signals.\"}\n" +
                    "       - Intent: 'tell me if carrier ABC is here' Data: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}] -> {\"response_text\": \"No match for fleet carrier ABC, My Lord.\", \"details\": \"No such carrier in local signals.\"}\n" +
                    "       - Intent: 'summarize local signals' Data: [{\"name\": \"Fleet Carrier XYZ\", \"type\": \"Carrier\"}, {\"name\": \"Distress Signal\", \"type\": \"USS\"}] -> {\"response_text\": \"Local signals: one fleet carrier, one distress signal, My Lord.\", \"details\": \"Carrier: XYZ, USS: Distress Signal\"}";

            JsonObject request = new JsonObject();
            request.addProperty("model", "grok-3-fast");
            request.addProperty("temperature", 0.7);
            request.addProperty("stream", false);

            JsonObject messageSystem = new JsonObject();
            messageSystem.addProperty("role", "system");
            messageSystem.addProperty("content", systemPrompt);

            JsonObject messageUser = new JsonObject();
            messageUser.addProperty("role", "user");
            messageUser.addProperty("content", "User intent: " + userIntent + "\nData: " + dataJson);

            request.add("messages", gson.toJsonTree(new Object[]{messageSystem, messageUser}));

            String jsonString = gson.toJson(request);
            logger.debug("xAI API call: [{}]", toDebugString(jsonString));

            try (var os = conn.getOutputStream()) {
                os.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String response;
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }

            // Strip BOM if present
            if (response.startsWith("\uFEFF")) {
                response = response.substring(1);
                logger.info("Stripped BOM from response");
            }

            logger.debug("xAI API response: [{}]", toDebugString(response));

            if (responseCode != 200) {
                String errorResponse = "";
                try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                    errorResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                } catch (Exception e) {
                    logger.warn("Failed to read error stream: {}", e.getMessage());
                }
                logger.error("xAI API error: {} - {}", responseCode, conn.getResponseMessage());
                logger.info("Error response body: {}", errorResponse);
                return "{\"response_text\": \"Analysis error, My Lord.\"}";
            }

            // Parse response
            JsonObject json;
            try {
                json = JsonParser.parseString(response).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.error("Failed to parse API response: [{}]", toDebugString(response), e);
                return "{\"response_text\": \"Analysis error, My Lord.\"}";
            }

            // Extract content safely
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                logger.error("No choices in API response: [{}]", toDebugString(response));
                return "{\"response_text\": \"Analysis error, My Lord.\"}";
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                logger.error("No message in API response choices: [{}]", toDebugString(response));
                return ERROR_REPONSE;
            }

            String content = message.get("content").getAsString();
            if (content == null) {
                logger.error("No content in API response message: [{}]", toDebugString(response));
                return ERROR_REPONSE;
            }

            logger.debug("API response content: [{}]", toDebugString(content));

            // Extract JSON from content
            String jsonContent;
            int jsonStart = content.indexOf("\n\n{");
            if (jsonStart != -1) {
                jsonContent = content.substring(jsonStart + 2); // Skip \n\n
            } else {
                jsonStart = content.indexOf("{");
                if (jsonStart == -1) {
                    logger.error("No JSON object found in content: [{}]", toDebugString(content));
                    return ERROR_REPONSE;
                }
                jsonContent = content.substring(jsonStart);
                try {
                    JsonParser.parseString(jsonContent);
                } catch (JsonSyntaxException e) {
                    logger.error("Invalid JSON object in content: [{}]", toDebugString(jsonContent), e);
                    return ERROR_REPONSE;
                }
            }

            logger.info("Extracted JSON content: [{}]", toDebugString(jsonContent));
            return jsonContent; // Return JSON string for AnalyzeDataHandler to parse
        } catch (Exception e) {
            logger.error("AI API call fatal error: {}", e.getMessage(), e);
            return "{\"response_text\": \"Analysis error. Check logs.\"}";
        }
    }

    private HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ConfigManager.getInstance().readSystemConfig().get("grok_key"));
        conn.setDoOutput(true);
        return conn;
    }

    private String toDebugString(String input) {
        if (input == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c < 32 || c == 127 || c == 0xFEFF) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
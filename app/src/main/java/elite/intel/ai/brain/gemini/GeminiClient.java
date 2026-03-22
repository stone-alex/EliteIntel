package elite.intel.ai.brain.gemini;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.BaseAiClient;
import elite.intel.ai.brain.Client;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class GeminiClient extends BaseAiClient implements Client {

    public static final String MODEL_FLASH = "gemini-3.1-flash-lite-preview";
    public static final String MODEL_FLASH_LITE = "gemini-2.5-flash-lite";

    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final GeminiClient instance = new GeminiClient();

    private volatile String currentModel = MODEL_FLASH;

    private GeminiClient() {
    }

    public static GeminiClient getInstance() {
        return instance;
    }

    // not used
    @Override
    public JsonObject createPrompt(int model, float temp) {
        return null;
    }

    @Override
    public JsonObject createPrompt(String model, float temp) {
        this.currentModel = model;
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", temp);
        generationConfig.addProperty("maxOutputTokens", 1024);
        JsonObject prompt = new JsonObject();
        prompt.add("generationConfig", generationConfig);
        return prompt;
    }

    @Override
    public JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("text_to_speech_response", message);
        return error;
    }

    @Override
    public JsonObject sendJsonRequest(String request) {
        JsonObject response = super.sendJsonRequest(request, getHttpURLConnection());
        if (response != null && response.has("usageMetadata")) {
            JsonObject usage = response.getAsJsonObject("usageMetadata");
            int promptTokens = usage.has("promptTokenCount") ? usage.get("promptTokenCount").getAsInt() : 0;
            int candidateTokens = usage.has("candidatesTokenCount") ? usage.get("candidatesTokenCount").getAsInt() : 0;
            EventBusManager.publish(new AppLogEvent(
                    "LLM Gemini [" + currentModel + "] in=" + promptTokens + " out=" + candidateTokens));
        }
        return response;
    }

    @Override
    public HttpURLConnection getHttpURLConnection() {
        try {
            String apiKey = SystemSession.getInstance().getAiApiKey();
            URI uri = URI.create(API_BASE_URL + currentModel + ":generateContent?key=" + apiKey);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(60_000);
            return conn;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts the generated text from a Gemini API response.
     * Response shape: candidates[0].content.parts[0].text
     */
    public String extractText(JsonObject response) {
        return response
                .getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }
}

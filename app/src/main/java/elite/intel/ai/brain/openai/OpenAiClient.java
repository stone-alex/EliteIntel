package elite.intel.ai.brain.openai;

import com.google.gson.JsonObject;
import elite.intel.ai.ConfigManager;
import elite.intel.session.SystemSession;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class OpenAiClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    public static final String MODEL = "gpt-4.1-mini"; // Fast, cost-effective model
    public static final int MAX_TOKENS_DEFAULT = 4096;
    private static OpenAiClient instance;

    private OpenAiClient() {
        // Private constructor for singleton
    }

    public static OpenAiClient getInstance() {
        if (instance == null) {
            instance = new OpenAiClient();
        }
        return instance;
    }

    JsonObject createRequestBodyHeader(String model) {
        float temp = SystemSession.getInstance()
                .getAIPersonality()
                .getTemperature();
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("temperature", temp);
        body.addProperty("max_tokens", MAX_TOKENS_DEFAULT);
        return body;
    }

    JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("response_text", message);
        return error;
    }

    HttpURLConnection getHttpURLConnection() throws IOException {
        URI uri = URI.create(API_URL);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY));
        conn.setRequestProperty("User-Agent", "EliteIntel/1.0");
        conn.setDoOutput(true);
        return conn;
    }
}
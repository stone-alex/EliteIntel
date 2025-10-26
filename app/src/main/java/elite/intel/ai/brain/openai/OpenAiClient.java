package elite.intel.ai.brain.openai;

import com.google.gson.JsonObject;
import elite.intel.ai.ConfigManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class OpenAiClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public static final String MODEL_GPT_4_1_MINI = "gpt-4.1-mini";
    //NOTE: Do not use nano LLM. It can't properly map commands or queries.

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

    JsonObject createRequestBodyHeader(String model, float temp) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", model);
        jsonObject.addProperty("temperature", temp);
        return jsonObject;
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
package elite.intel.ai.brain.grok;


import com.google.gson.JsonObject;
import elite.intel.ai.ConfigManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GrokClient {

    private GrokClient() {
    }

    public static GrokClient getInstance() {
        return instance;
    }


    /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // WARNING: This is LLM specific implementation. If you naively swap this URL to another LLM expect massive failures.
    // If you want to use a different LLM, see documentation in README.md. for the elite.intel.ai.brain package
    // You must provide an implementation that supports current functionality and aligns with the apps' architecture.
    // See this package for inspiration on how to implement your own LLM client.
    private static final String API_URL = "https://api.x.ai/v1/chat/completions";

    private static final String MODEL = "grok-3-fast";

    public static final double TEMPERATURE = 0.7;
    public static final boolean IS_STREAM = false;


    private static final GrokClient instance = new GrokClient();


    JsonObject createRequestBodyHeader() {
        JsonObject header = new JsonObject();
        header.addProperty("model", MODEL);
        header.addProperty("temperature", TEMPERATURE);
        header.addProperty("stream", IS_STREAM);
        return header;
    }

    JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("response_text", message);
        return error;
    }

    HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY));
        conn.setDoOutput(true);
        return conn;
    }
}
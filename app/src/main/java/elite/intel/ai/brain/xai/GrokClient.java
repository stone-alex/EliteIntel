package elite.intel.ai.brain.xai;


import com.google.gson.JsonObject;
import elite.intel.ai.brain.Client;
import elite.intel.session.SystemSession;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class GrokClient implements Client {

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


    /// Grok 4.1
    // public static final String MODEL_GROK_REASONING = "grok-4-1-fast-reasoning";
    // public static final String MODEL_GROK_NON_REASONING = "grok-4-1-fast-non-reasoning";

    /// Grok 4
    // public static final String MODEL_GROK_REASONING = "grok-4-fast-reasoning";
    // public static final String MODEL_GROK_NON_REASONING = "grok-4-fast-non-reasoning";

    /// Grok 3
    // public static final String MODEL_GROK_NON_REASONING = "grok-3-fast";
    
    public static final String MODEL_GROK_NON_REASONING = "grok-3-mini";
    public static final String MODEL_GROK_REASONING = "grok-3-fast";


    ///
    public static final boolean IS_STREAM = false;
    private static final GrokClient instance = new GrokClient();

    //not used
    @Override public JsonObject createPrompt(int model, float temp) {
        return null;
    }

    @Override public JsonObject createPrompt(String model, float temp) {

        JsonObject header = new JsonObject();
        header.addProperty("model", model);
        header.addProperty("temperature", temp);
        header.addProperty("stream", IS_STREAM);
        return header;
    }

    @Override public JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("response_text", message);
        return error;
    }

    @Override public HttpURLConnection getHttpURLConnection() throws IOException {
        URI uri = URI.create(API_URL);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + SystemSession.getInstance().getAiApiKey());
        conn.setDoOutput(true);
        return conn;
    }
}
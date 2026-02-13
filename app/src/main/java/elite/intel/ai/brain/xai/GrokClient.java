package elite.intel.ai.brain.xai;


import com.google.gson.JsonObject;
import elite.intel.ai.brain.Client;
import elite.intel.ai.brain.BaseAiClient;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.LlmMetadata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class GrokClient extends BaseAiClient implements Client {

    /// Grok 3
    // public static final String MODEL_GROK_NON_REASONING = "grok-3-fast";

    public static final String MODEL_GROK_NON_REASONING = "grok-4-1-fast-non-reasoning";
    public static final String MODEL_GROK_REASONING = "grok-4-fast-reasoning";
    ///
    public static final boolean IS_STREAM = false;

    /// Grok 4.1
    // public static final String MODEL_GROK_REASONING = "grok-4-1-fast-reasoning";
    // public static final String MODEL_GROK_NON_REASONING = "grok-4-1-fast-non-reasoning";

    /// Grok 4
    // public static final String MODEL_GROK_REASONING = "grok-4-fast-reasoning";
    // public static final String MODEL_GROK_NON_REASONING = "grok-4-fast-non-reasoning";
    private static final String API_URL = "https://api.x.ai/v1/chat/completions";
    private static final GrokClient instance = new GrokClient();


    private GrokClient() {
        //
    }

    public static GrokClient getInstance() {
        return instance;
    }

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

    @Override public JsonObject sendJsonRequest(String request) {
        JsonObject response = super.sendJsonRequest(request, getHttpURLConnection());
        LlmMetadata meta = GsonFactory.getGson().fromJson(response, LlmMetadata.class);
        EventBusManager.publish(new AppLogEvent("LLM: " + meta));
        return response;
    }


    @Override public HttpURLConnection getHttpURLConnection() {
        try {
            URI uri = URI.create(API_URL);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + SystemSession.getInstance().getAiApiKey());
            conn.setDoOutput(true);
            return conn;
        } catch (IOException noConnection) {
            throw new RuntimeException(noConnection);
        }
    }
}
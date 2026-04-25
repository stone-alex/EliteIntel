package elite.intel.ai.brain.xai;


import com.google.gson.JsonObject;
import elite.intel.ai.brain.BaseAiClient;
import elite.intel.ai.brain.Client;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.LlmMetadata;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GrokClient extends BaseAiClient implements Client {

    /// use the same non-reasoning model for commands and queries for now.
    public static final String MODEL_GROK_NON_REASONING = "grok-4-1-fast-non-reasoning";
    public static final String MODEL_GROK_REASONING = "grok-4-1-fast-non-reasoning";

    ///
    public static final boolean IS_STREAM = false;
    private static final String API_URL = "https://api.x.ai/v1/chat/completions";
    private static final GrokClient instance = new GrokClient();
    private static final PlayerSession playerSession = PlayerSession.getInstance();


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
        error.addProperty("text_to_speech_response", message);
        return error;
    }

    @Override public JsonObject sendJsonRequest(String request) {
        JsonObject response = super.sendJsonRequest(buildRequest(request));
        LlmMetadata meta = GsonFactory.getGson().fromJson(response, LlmMetadata.class);
        EventBusManager.publish(new AppLogEvent("LLM: " + meta));
        return response;
    }

    HttpRequest buildRequest(String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SystemSession.getInstance().getAiApiKey())
                .header("x-grok-conv-id", playerSession.getUUD())
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
package elite.intel.ai.brain.ollama;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.BaseAiClient;
import elite.intel.ai.brain.Client;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.OllamaMetadata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class OllamaClient extends BaseAiClient implements Client {
    // qwen2.5:14b working but .5 sec on command response.


    public static final Integer MODEL_OLLAMA_SMALL = 1;
    public static final Integer MODEL_OLLAMA = 2;
    private static final OllamaClient INSTANCE = new OllamaClient();

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();

    private OllamaClient() {
        //
    }

    public static OllamaClient getInstance() {
        return INSTANCE;
    }

    private String getBaseUrl() {
        return playerSession.getLocalLlmAddress();
    }

    @Override
    public JsonObject createPrompt(int model, float temp) {

        String localLlmCommandModel = systemSession.getLocalLlmCommandModel();
        String localLlmQueryModel = systemSession.getLocalLlmQueryModel();

        JsonObject request = new JsonObject();
        request.addProperty("model", model == 1 ? localLlmCommandModel : localLlmQueryModel);
        request.addProperty("temperature", temp);
        request.addProperty("stream", false);
        request.addProperty("format", "json");
        request.addProperty("stream", false);
        request.addProperty("think", false);
        request.addProperty("num_ctx", 128000);

        /// OLAMA tricks.
        /// Accuracy / Coherence
//        request.addProperty("top_k", 5);            // default ~40–50, lower = more focused (try 20–30)
//        request.addProperty("mirostat", 0);             // 0=off, 1=mirostat, 2=mirostat 2.0 (best for stable JSON output)
//        request.addProperty("mirostat_tau", 0.0f);      // target perplexity (3.0–6.0, higher = more creative)
//        request.addProperty("mirostat_eta", 0.1f);      // learning rate (default 0.1 is fine)
//        request.addProperty("top_p", 0.9);                // default 0.9, lower = less random (0.7–0.9 sweet spot)
//        request.addProperty("repeat_penalty", 1.1f);    // 1.05–1.2 discourages loops/repetition

        /// Speed / VRAM / Performance
        request.addProperty("num_predict", -1);         // max tokens to generate (default -1 = unlimited, set 256–1024)
        request.addProperty("num_keep", 4000);             // tokens from prompt to keep in KV cache (0 = default, usually all)
        request.addProperty("num_thread", 0);           // 0 = auto (good), or set to physical cores if you want to limit CPU

        return request;
    }

    //not used
    @Override public JsonObject createPrompt(String model, float temp) {
        return null;
    }

    @Override
    public JsonObject createErrorResponse(String message) {
        JsonObject err = new JsonObject();
        err.addProperty("response_text", message);
        return err;
    }

    @Override public JsonObject sendJsonRequest(String request) {
        JsonObject response = super.sendJsonRequest(request, getHttpURLConnection());
        OllamaMetadata metadata = GsonFactory.getGson().fromJson(response, OllamaMetadata.class);
        EventBusManager.publish(new AppLogEvent("AI: Model " + metadata));
        return response;
    }

    @Override
    public HttpURLConnection getHttpURLConnection() {
        try {
            String url = getBaseUrl() + "/api/chat";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(115_000);
            conn.setReadTimeout(1_100_000);
            return conn;
        } catch (IOException noConnection) {
            throw new RuntimeException(noConnection);
        }
    }
}
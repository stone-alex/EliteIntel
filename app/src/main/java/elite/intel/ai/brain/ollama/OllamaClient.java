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
    public static final Integer MODEL_COMMANDS = 1;
    public static final Integer MODEL_QUERIES = 2;
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
        request.addProperty("think", false);
        // num_ctx intentionally omitted — governed by Modelfile (tulu3-gaming: 2048 commands, 4096 queries)

        /// OLLAMA tricks.
        /// Strongest "follow instructions + output clean JSON" preset for ~8–13B models
        request.addProperty("mirostat", 2);           // mirostat 2.0 - best for controlled output
        request.addProperty("mirostat_tau", 3.5f);    // 3.0–4.5 range; 3.5 is a common sweet spot
        request.addProperty("mirostat_eta", 0.1f);    // leave at default
        request.addProperty("top_k", 20);             // 15–30 → very low values help JSON a lot
        request.addProperty("repeat_penalty", 1.12f); // 1.08–1.15 → prevents repeating keys or structure

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
            String url = getBaseUrl();
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
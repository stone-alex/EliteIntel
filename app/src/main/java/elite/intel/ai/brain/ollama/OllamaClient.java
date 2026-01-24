package elite.intel.ai.brain.ollama;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.Client;
import elite.intel.session.PlayerSession;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class OllamaClient implements Client {

    public static final String MODEL_OLLAMA = "qwen2.5:14b";
    public static final String MODEL_OLLAMA_SMALL = "qwen2.5:14b";
    private static final OllamaClient INSTANCE = new OllamaClient();

    private final PlayerSession playerSession = PlayerSession.getInstance();

    private OllamaClient() {
    }

    public static OllamaClient getInstance() {
        return INSTANCE;
    }

    private String getBaseUrl() {
        return playerSession.getLocalLlmAddress();
    }

    @Override
    public JsonObject createPrompt(String model, float temp) {
        JsonObject request = new JsonObject();
        request.addProperty("model", model);
        request.addProperty("temperature", 0.5);
        request.addProperty("stream", false);
        request.addProperty("format", "json");
        request.addProperty("num_ctx", 16384);
        request.addProperty("stream", false);
        request.addProperty("think", false);

        /// OLAMA tricks.
        /// Accuracy / Coherence
//        request.addProperty("top_k", 5);            // default ~40–50, lower = more focused (try 20–30)
//        request.addProperty("mirostat", 0);             // 0=off, 1=mirostat, 2=mirostat 2.0 (best for stable JSON output)
//        request.addProperty("mirostat_tau", 0.0f);      // target perplexity (3.0–6.0, higher = more creative)
//        request.addProperty("mirostat_eta", 0.1f);      // learning rate (default 0.1 is fine)
        request.addProperty("top_p", 0.9);                // default 0.9, lower = less random (0.7–0.9 sweet spot)
        request.addProperty("repeat_penalty", 1.1f);    // 1.05–1.2 discourages loops/repetition

        /// Speed / VRAM / Performance
        request.addProperty("num_predict", 2048);         // max tokens to generate (default -1 = unlimited, set 256–1024)
        request.addProperty("num_keep", 0);             // tokens from prompt to keep in KV cache (0 = default, usually all)
        request.addProperty("num_thread", 0);           // 0 = auto (good), or set to physical cores if you want to limit CPU

        return request;
    }

    @Override
    public JsonObject createErrorResponse(String message) {
        JsonObject err = new JsonObject();
        err.addProperty("response_text", message);
        return err;
    }

    @Override
    public HttpURLConnection getHttpURLConnection() throws IOException {
        String url = getBaseUrl() + "/api/chat";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(115_000);
        conn.setReadTimeout(1_100_000);
        return conn;
    }
}
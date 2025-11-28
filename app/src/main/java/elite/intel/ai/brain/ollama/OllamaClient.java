package elite.intel.ai.brain.ollama;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.Client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class OllamaClient implements Client {

    public static final String MODEL_TINYLAMA = "tinyllama";
    public static final String MODEL_PFI_MINI = "phi3:mini";

    private static final OllamaClient INSTANCE = new OllamaClient();

    private OllamaClient() {
    }

    public static OllamaClient getInstance() {
        return INSTANCE;
    }

    // Configurable via SystemSession (fallback to localhost)
    private String getBaseUrl() {
        String url = "http://127.0.0.1:11434"; //TODO: change to env variable or something
        return url != null && !url.isBlank() ? url.trim() : "http://127.0.0.1:11434";
    }

    @Override
    public JsonObject createRequestBodyHeader(String model, float temp) {
        JsonObject header = new JsonObject();
        header.addProperty("model", model);
        header.addProperty("temperature", temp);
        header.addProperty("stream", false);           // we don’t use streaming
        return header;
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
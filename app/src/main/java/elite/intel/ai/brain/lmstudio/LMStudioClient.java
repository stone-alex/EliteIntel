package elite.intel.ai.brain.lmstudio;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.BaseAiClient;
import elite.intel.ai.brain.Client;
import elite.intel.session.SystemSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LMStudioClient extends BaseAiClient implements Client {

    private static final Logger log = LogManager.getLogger(LMStudioClient.class);

    public static final Integer MODEL_COMMANDS = 1;
    public static final Integer MODEL_QUERIES = 2;

    private static final LMStudioClient INSTANCE = new LMStudioClient();
    private final SystemSession systemSession = SystemSession.getInstance();

    private LMStudioClient() {
    }

    public static LMStudioClient getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonObject createPrompt(int model, float temp) {
        boolean isQueryModel = model == MODEL_QUERIES;

        JsonObject request = new JsonObject();
        request.addProperty("model", isQueryModel
                ? systemSession.getLocalLlmQueryModel().trim()
                : systemSession.getLocalLlmCommandModel().trim());
        request.addProperty("temperature", temp);
        request.addProperty("max_tokens", isQueryModel ? 512 : 200);
        request.addProperty("stream", false);
        return request;
    }

    // not used
    @Override
    public JsonObject createPrompt(String model, float temp) {
        return null;
    }

    @Override
    public JsonObject createErrorResponse(String message) {
        JsonObject err = new JsonObject();
        err.addProperty("text_to_speech_response", message);
        return err;
    }

    @Override
    public synchronized JsonObject sendJsonRequest(String request) {
        return super.sendJsonRequest(request, getHttpURLConnection());
    }

    @Override
    public HttpURLConnection getHttpURLConnection() {
        try {
            String url = systemSession.getLocalLlmAddress();
            log.info("LM Studio connecting to: {}", url);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(115_000);
            conn.setReadTimeout(1_100_000);
            return conn;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

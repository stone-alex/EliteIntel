package elite.intel.ai.brain.inference.lmstudio;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.BaseAiClient;
import elite.intel.ai.brain.Client;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.ui.event.LlmUsageEvent;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.LlmMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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
        request.addProperty("max_tokens", isQueryModel ? 1024 : 512);
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
        JsonObject response = super.sendJsonRequest(buildRequest(request));
        LlmMetadata meta = GsonFactory.getGson().fromJson(response, LlmMetadata.class);
        EventBusManager.publish(new AppLogEvent("LM Studio: " + meta));
        if (meta != null && meta.usage() != null) {
            EventBusManager.publish(new LlmUsageEvent("LM Studio",
                    meta.model() != null ? meta.model() : "local",
                    meta.usage().promptTokens(), meta.usage().completionTokens(), 0, 0));
        }
        return response;
    }

    private HttpRequest buildRequest(String body) {
        String url = systemSession.getLocalLlmAddress();
        log.info("LM Studio connecting to: {}", url);
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(1_100))
                .build();
    }
}
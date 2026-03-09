package elite.intel.ai.brain.anthropic;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.BaseAiClient;
import elite.intel.ai.brain.Client;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client for the Anthropic (Claude) Messages API.
 * <p>
 * Key differences from Ollama/OpenAI:
 * - Auth via "x-api-key" header (not Bearer token)
 * - Requires "anthropic-version" header
 * - "system" prompt is a TOP-LEVEL field, NOT a messages[] entry
 * - Response content is at: response.content[0].text
 * <p>
 * The system-prompt extraction from the shared JsonArray is handled in
 * AnthropicCommandEndPoint before calling sendJsonRequest(), so this
 * client receives a body that is already correctly shaped for the API.
 */
public class AnthropicClient extends BaseAiClient implements Client {


    private static final Logger log = LogManager.getLogger(AnthropicClient.class);


    // Model identifiers - expose all so callers can pick
    public static final String MODEL_COMMAND_MODEL = "claude-haiku-4-5-20251001";    // fastest, cheapest - ideal for command parsing
    public static final String MODEL_ANALYSIS_MODEL = "claude-haiku-4-5-20251001";   //"claude-sonnet-4-6";           // fast, balanced - best for analysis/chat
    //public static final String MODEL_OPUS = "claude-opus-4-6";               // most capable

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";

    // Claude has a hard max_tokens requirement - must be explicit
    private static final int DEFAULT_MAX_TOKENS = 1024;

    private static final AnthropicClient INSTANCE = new AnthropicClient();

    private AnthropicClient() {
        // singleton
    }

    public static AnthropicClient getInstance() {
        return INSTANCE;
    }


    @Override public JsonObject createPrompt(int model, float temp) {
        return createPrompt(model == 1 ? MODEL_COMMAND_MODEL : MODEL_ANALYSIS_MODEL, temp);
    }

    @Override
    public JsonObject createPrompt(String model, float temp) {
        JsonObject request = new JsonObject();
        request.addProperty("model", model);
        request.addProperty("max_tokens", DEFAULT_MAX_TOKENS);
        request.addProperty("temperature", temp);
        return request;
    }


    @Override
    public JsonObject createErrorResponse(String message) {
        JsonObject err = new JsonObject();
        err.addProperty("response_text", message);
        return err;
    }

    /**
     * Send the request and log a brief token-usage summary to the app log.
     * Claude's response envelope:
     * {
     * "id": "...",
     * "type": "message",
     * "role": "assistant",
     * "content": [{ "type": "text", "text": "..." }],
     * "usage": { "input_tokens": N, "output_tokens": M }
     * }
     */
    @Override
    public JsonObject sendJsonRequest(String request) {
        try {
            HttpURLConnection conn = getHttpURLConnection();

            byte[] body = request.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(body.length));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                // READ THE ERROR STREAM
                InputStream err = conn.getErrorStream();
                String errorBody = err != null
                        ? new String(err.readAllBytes(), StandardCharsets.UTF_8)
                        : "(no error body)";
                log.error("Anthropic HTTP {} – error body: {}", status, errorBody);
                EventBusManager.publish(new AiVoxResponseEvent(errorBody));
                return createErrorResponse("HTTP " + status);
            } else {
                JsonObject response = super.sendJsonRequest(request, getHttpURLConnection());

                if (response != null && response.has("usage")) {
                    JsonObject usage = response.getAsJsonObject("usage");
                    int in = usage.has("input_tokens") ? usage.get("input_tokens").getAsInt() : 0;
                    int out = usage.has("output_tokens") ? usage.get("output_tokens").getAsInt() : 0;
                    int cached = usage.has("cache_read_input_tokens") ? usage.get("cache_read_input_tokens").getAsInt() : 0;
                    int written = usage.has("cache_creation_input_tokens") ? usage.get("cache_creation_input_tokens").getAsInt() : 0;
                    EventBusManager.publish(new AppLogEvent(
                            "AI: Claude – in= " + in + " out= " + out +
                            (cached > 0 ? " cache_read= " + cached : "") +
                            (written > 0 ? " cache_written= " + written : "") +
                            " tokens"));
                }
                return response;
            }

            // normal success path...

        } catch (Exception e) {
            log.error("Request failed", e);
            return createErrorResponse(e.getMessage());
        }
    }

    @Override
    public HttpURLConnection getHttpURLConnection() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", SystemSession.getInstance().getAiApiKey());
            conn.setRequestProperty("anthropic-version", API_VERSION);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(120_000);
            return conn;
        } catch (IOException e) {
            throw new RuntimeException("Failed to open Anthropic connection", e);
        }
    }

    // -----------------------------------------------------------------------
    // Response helpers – called by endpoint classes
    // -----------------------------------------------------------------------

    /**
     * Extract the text content from a raw Claude API response envelope.
     * Returns null if the response is malformed.
     */
    public String extractText(JsonObject response) {
        try {
            return response
                    .getAsJsonArray("content")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

}
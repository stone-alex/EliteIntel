package elite.intel.ai.brain;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class BaseAiClient {

    private volatile HttpURLConnection currentConnection = null;
    private volatile Thread currentRequestThread = null;  // optional, for interrupt()


    public BaseAiClient() {
    }

    public JsonObject createErrorResponse(String message) {
        JsonObject err = new JsonObject();
        err.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, message);
        return err;
    }

    public void cancelCurrentRequest() {
        HttpURLConnection conn = currentConnection;
        if (conn != null) {
            conn.disconnect();  // safe to call from any thread; closes socket → Ollama aborts
            currentConnection = null;  // clear after cancel
        }
        Thread t = currentRequestThread;
        if (t != null) {
            t.interrupt();  // optional: wake up if blocked on read
        }
    }

    // Helper to set/reset connection (call before/after sending)
    private void setCurrentConnection(HttpURLConnection conn, Thread thread) {
        currentConnection = conn;
        currentRequestThread = thread;
    }

    private void clearCurrentConnection() {
        currentConnection = null;
        currentRequestThread = null;
    }

    public JsonObject sendJsonRequest(String request, HttpURLConnection conn) {
        cancelCurrentRequest();
        Thread currentThread = Thread.currentThread();  // or pass from caller if async

        try {
            setCurrentConnection(conn, currentThread);  // ← key: register for cancel

            conn.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();

            int code = conn.getResponseCode();
            if (code != 200) {
                return createErrorResponse("HTTP " + code);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return JsonParser.parseString(sb.toString()).getAsJsonObject();
            }
        } catch (IOException e) {
            if (e.getMessage() != null && (e.getMessage().contains("Socket closed") || e instanceof java.net.SocketException)) {
                return createErrorResponse("LLM Call Failed");
            }
            return createErrorResponse("Request failed: " + e.getMessage());
        } finally {
            clearCurrentConnection();  // always clean up
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ignored) {
                }
            }
        }
    }
}

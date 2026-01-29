package elite.intel.ai.brain;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface Client {
    JsonObject createPrompt(int model, float temp);
    JsonObject createPrompt(String model, float temp);
    JsonObject createErrorResponse(String message);
    JsonObject sendJsonRequest(String request);
    HttpURLConnection getHttpURLConnection() throws IOException;
}

package elite.intel.ai.brain;

import com.google.gson.JsonObject;

public interface Client {
    JsonObject createPrompt(int model, float temp);
    JsonObject createPrompt(String model, float temp);
    JsonObject createErrorResponse(String message);
    JsonObject sendJsonRequest(String request);
}

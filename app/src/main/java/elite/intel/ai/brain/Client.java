package elite.intel.ai.brain;

import com.google.gson.JsonObject;

public interface Client {

    /// Local LLMs
    JsonObject createPrompt(int model, float temp);

    /// Cloud LLMs
    JsonObject createPrompt(String model, float temp);

    JsonObject createErrorResponse(String message);

    JsonObject sendJsonRequest(String request);
}

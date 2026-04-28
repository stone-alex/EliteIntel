package elite.intel.ai.brain.actions.handlers.query;

import com.google.gson.JsonObject;

public interface QueryHandler {
    JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception;
}
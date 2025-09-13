package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;

public interface CommandHandler {
    void handle(JsonObject params, String responseText);
}
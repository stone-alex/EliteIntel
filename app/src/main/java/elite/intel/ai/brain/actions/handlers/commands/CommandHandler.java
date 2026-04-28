package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;

public interface CommandHandler {
    void handle(String action, JsonObject params, String responseText);
}
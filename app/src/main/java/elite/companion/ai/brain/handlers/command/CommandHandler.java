package elite.companion.ai.brain.handlers.command;

import com.google.gson.JsonObject;

public interface CommandHandler {
    void handle(JsonObject params, String responseText);
}
package elite.companion.comms.handlers;

import com.google.gson.JsonObject;

public interface CommandHandler {
    void handle(JsonObject params, String responseText);
}
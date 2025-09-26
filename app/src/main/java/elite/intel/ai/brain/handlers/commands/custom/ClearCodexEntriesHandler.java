package elite.intel.ai.brain.handlers.commands.custom;


import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.session.PlayerSession;

public class ClearCodexEntriesHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession.getInstance().clearCodexEntries();
    }
}

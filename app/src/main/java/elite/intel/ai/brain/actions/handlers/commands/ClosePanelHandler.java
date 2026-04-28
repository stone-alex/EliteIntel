package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;

public class ClosePanelHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        UiNavCommon.close();
    }
}
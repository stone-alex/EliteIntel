package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.session.PlayerSession;

public class ClearMiningTargetsHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession.getInstance().clearMiningTargets();
        EventBusManager.publish(new VocalisationRequestEvent("Mining targets cleared."));
    }
}

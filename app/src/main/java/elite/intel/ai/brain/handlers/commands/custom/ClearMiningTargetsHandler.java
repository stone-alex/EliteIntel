package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.session.PlayerSession;

public class ClearMiningTargetsHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession.getInstance().clearMiningTargets();
        EventBusManager.publish(new AiVoxResponseEvent("Mining targets cleared."));
    }
}

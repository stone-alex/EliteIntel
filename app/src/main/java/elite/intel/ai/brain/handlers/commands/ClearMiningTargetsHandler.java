package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class ClearMiningTargetsHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession.getInstance().clearMiningTargets();
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Mining targets cleared."));
    }
}

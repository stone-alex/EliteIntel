package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class DisableAllAnnouncementHandler implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        playerSession.setDiscoveryAnnouncementOn(false);
        playerSession.setMiningAnnouncementOn(false);
        playerSession.setNavigationAnnouncementOn(false);
        playerSession.setRadioTransmissionOn(false);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("All announcements are disabled."));
    }
}

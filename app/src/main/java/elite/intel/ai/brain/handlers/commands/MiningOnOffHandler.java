package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class MiningOnOffHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {

        boolean isOn = params.get("state").getAsBoolean();

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setMiningAnnouncementOn(isOn);
        EventBusManager.publish(new AiVoxResponseEvent("Mining Announcements: " + (isOn ? "On" : "Off")));
    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.EventBusManager;

public class ChangeTradeProfileAllowPermitSystemsHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        boolean isOn = params.get("state").getAsBoolean();
        TradeProfileManager profileManager = TradeProfileManager.getInstance();
        if(profileManager.setAllowPermit(isOn)) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Permit systems: " + (isOn ? "On" : "Off")));
        }
    }
}

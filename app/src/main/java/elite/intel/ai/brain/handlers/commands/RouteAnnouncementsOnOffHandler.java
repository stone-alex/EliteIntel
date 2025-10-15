package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class RouteAnnouncementsOnOffHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        boolean isOn = params.get("state").getAsBoolean();
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setRouteAnnouncementOn(isOn);
        EventBusManager.publish(new AiVoxResponseEvent("Route Announcements: " + (isOn ? "On" : "Off")));
    }
}

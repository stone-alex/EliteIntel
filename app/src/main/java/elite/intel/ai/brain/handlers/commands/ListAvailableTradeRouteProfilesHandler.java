package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;

public class ListAvailableTradeRouteProfilesHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        EventBusManager.publish(
                new MissionCriticalAnnouncementEvent("Available Trade profile parameters are: Starting Capital, Distance from entry, Maximum stops, Allow Permit protected systems, Allow Planetary Ports, Allow Fleet Carriers and Allow Prohibited cargo.")
        );
    }
}

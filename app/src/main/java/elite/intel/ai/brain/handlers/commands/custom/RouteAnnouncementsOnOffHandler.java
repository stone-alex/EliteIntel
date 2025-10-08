package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

import static elite.intel.util.json.JsonParameterExtractor.extractParameter;

public class RouteAnnouncementsOnOffHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CustomCommands.ROUTE_ON_OFF.getPlaceholder(), params);

        if(jsonElement == null) {return;}

        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString()) || "true".equalsIgnoreCase(jsonElement.getAsString());
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setRouteAnnouncementOn(isOn);
        EventBusManager.publish(new AiVoxResponseEvent("Route Announcements: " + (isOn ? "On" : "Off")));
    }
}

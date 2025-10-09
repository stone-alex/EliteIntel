package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

import static elite.intel.util.json.JsonParameterExtractor.extractParameter;

public class DiscoveryOnOffHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {

        JsonElement jsonElement = extractParameter(Commands.DISCOVERY_ON_OFF.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString()) || "true".equalsIgnoreCase(jsonElement.getAsString());

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setDiscoveryAnnouncementOn(isOn);
        EventBusManager.publish(new AiVoxResponseEvent("Discovery Announcements: " + (isOn ? "On" : "Off")));

    }
}

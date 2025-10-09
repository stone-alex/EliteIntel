package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;

import static elite.intel.util.json.JsonParameterExtractor.extractParameter;

public class NavigationOnOffHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();

        JsonElement jsonElement = extractParameter(Commands.NAVIGATION_ON_OFF.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString()) || "true".equalsIgnoreCase(jsonElement.getAsString());

        TargetLocation tracking = playerSession.getTracking();
        tracking.setEnabled(isOn);
        playerSession.setTracking(tracking);

        EventBusManager.publish(new AiVoxResponseEvent("Navigation guidance: " + (tracking.isEnabled() ? "On" : "Off")));
    }

}

package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;

import static elite.intel.util.json.JsonParameterExtractor.extractParameter;

public class NavigationOnOffHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        JsonElement jsonElement = extractParameter(CustomCommands.NAVIGATION_ON_OFF.getPlaceholder(), params);
        boolean isOn = "on".equalsIgnoreCase(jsonElement.getAsString());

        PlayerSession playerSession = PlayerSession.getInstance();
        TargetLocation tracking = playerSession.getTracking();
        tracking.setEnabled(isOn);
        tracking.setRequestedTime(System.currentTimeMillis());
        playerSession.setTracking(tracking);
    }

}

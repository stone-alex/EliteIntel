package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;

public class NavigationOnOffHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {

        PlayerSession playerSession = PlayerSession.getInstance();
        TargetLocation tracking = playerSession.getTracking();
        tracking.setEnabled(!tracking.isEnabled());
        tracking.setRequestedTime(System.currentTimeMillis());
        playerSession.setTracking(tracking);
        EventBusManager.publish(new VocalisationRequestEvent("Navigation guidance: " + (tracking.isEnabled() ? "On" : "Off")));
    }

}

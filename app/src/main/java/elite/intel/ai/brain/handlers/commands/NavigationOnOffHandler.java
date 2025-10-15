package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;

public class NavigationOnOffHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();


        boolean isOn = params.get("state").getAsBoolean();

        TargetLocation tracking = playerSession.getTracking();
        tracking.setEnabled(isOn);
        playerSession.setTracking(tracking);

        EventBusManager.publish(new AiVoxResponseEvent("Navigation guidance: " + (tracking.isEnabled() ? "On" : "Off")));
    }

}

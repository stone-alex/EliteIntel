package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;

public class NavigationOnOffHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setTracking(new TargetLocation(false));
        EventBusManager.publish(new AiVoxResponseEvent("Navigation guidance off..."));
    }

}

package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.session.PlayerSession;

public class StopOrCancelTrackingHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {
        PlayerSession.getInstance().setTracking(new TargetLocation());
    }
}

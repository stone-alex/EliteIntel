package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.HuntingGroundManager;
import elite.intel.gameapi.EventBusManager;

public class ClearHuntingGroundsHandler implements CommandHandler {

    private final HuntingGroundManager missionDataManager = HuntingGroundManager.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        missionDataManager.clear();
        EventBusManager.publish(new AiVoxResponseEvent("Hunting ground records cleared"));
    }
}

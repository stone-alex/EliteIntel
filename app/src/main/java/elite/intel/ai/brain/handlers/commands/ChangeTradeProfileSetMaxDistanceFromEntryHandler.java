package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.StringUtls;

public class ChangeTradeProfileSetMaxDistanceFromEntryHandler implements CommandHandler{

    @Override public void handle(String action, JsonObject params, String responseText) {
        Integer distanceFromEntry = StringUtls.getIntSafely(params.get("key").getAsString());

        if(distanceFromEntry == null){
            EventBusManager.publish(new AiVoxResponseEvent("Invalid distance from entry. Try again."));
            return;
        }

        TradeProfileManager manager = TradeProfileManager.getInstance();
        manager.setDistanceFromSystemEntry(distanceFromEntry);
        EventBusManager.publish(new AiVoxResponseEvent("Distance from system entry set to " + distanceFromEntry));
    }
}

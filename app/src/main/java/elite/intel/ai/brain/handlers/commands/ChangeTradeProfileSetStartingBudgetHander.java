package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.StringUtls;

public class ChangeTradeProfileSetStartingBudgetHander implements CommandHandler{


    @Override public void handle(String action, JsonObject params, String responseText) {
        Integer budget = StringUtls.getIntSafely(params.get("key").getAsString());
        if (budget == null) {
           EventBusManager.publish(new AiVoxResponseEvent("Invalid starting budget. Try again."));
           return;
        }

        TradeProfileManager manager = TradeProfileManager.getInstance();
        manager.setStartingCapitol(budget);
        EventBusManager.publish(new AiVoxResponseEvent("Starting budget set to " + budget+" credits."));
    }
}

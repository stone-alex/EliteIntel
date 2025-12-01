package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.StringUtls;

public class ChangeTradeProfileSetMaxStopsHandler implements CommandHandler {
    @Override public void handle(String action, JsonObject params, String responseText) {
        Integer numberOfStops = StringUtls.getIntSafely(params.get("key").getAsString());

        if (numberOfStops == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Invalid number of stops. Try again."));
            return;
        }

        TradeProfileManager profileManager = TradeProfileManager.getInstance();
        profileManager.setMaximumStops(numberOfStops);
        EventBusManager.publish(new AiVoxResponseEvent("Maximum stops set to " + numberOfStops));
    }
}

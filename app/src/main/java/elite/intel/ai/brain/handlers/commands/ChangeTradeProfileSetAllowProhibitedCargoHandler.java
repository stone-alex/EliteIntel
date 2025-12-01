package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.EventBusManager;

public class ChangeTradeProfileSetAllowProhibitedCargoHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        boolean isOn = params.get("state").getAsBoolean();
        TradeProfileManager profileManager = TradeProfileManager.getInstance();
        profileManager.setAllowProhibitedCargo(isOn);
        EventBusManager.publish(new AiVoxResponseEvent("Prohibited cargo: " + (isOn ? "On" : "Off")));
    }
}

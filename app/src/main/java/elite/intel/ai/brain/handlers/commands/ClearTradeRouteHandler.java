package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;

public class ClearTradeRouteHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        TradeRouteManager tradeRouteManager = TradeRouteManager.getInstance();
        tradeRouteManager.clear();
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Trade route cleared."));

    }
}

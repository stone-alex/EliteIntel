package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.traderoute.TradeRouteResponse;
import elite.intel.session.PlayerSession;

public class CalculateTradeRouteHandler implements CommandHandler {

    private final TradeRouteManager tradeRouteManager = TradeRouteManager.getInstance();
    private final TradeProfileManager profileManager = TradeProfileManager.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        if (!profileManager.hasCargoCapacity()) {
            EventBusManager.publish(new AiVoxResponseEvent("Please change to ship with cargo capacity"));
            return;
        }

        if (profileManager.getCriteria().getStartingCapital() == 0) {
            String shipName = playerSession.getShipLoadout().getShipName();
            StringBuilder sb = new StringBuilder();
            sb.append(" There is no trading profile for " + shipName + " You will have to set it once per cargo ship you own.");
            sb.append(" To set a trading profile, say: Change trading profile, followed by profile parameter. Available parameters are:\n");
            sb.append(" Starting Capital, distance from entry, maximum stops, allow Permit protected systems, allow Planetary Ports, allow Fleet Carrier allow Prohibited cargo.");
            sb.append(" Example: change trading profile, set startingCapital 100000");
            sb.append(" Please set one parameter at a time. Ask me to list trade profile parameters if you need help.");
            EventBusManager.publish(
                    new AiVoxResponseEvent(sb.toString())
            );
            return;
        }

        if (profileManager.getCriteria().getMaxHops() == 0){
            String shipName = playerSession.getShipLoadout().getShipName();
            EventBusManager.publish(new AiVoxResponseEvent("There is no number of stops set for " + shipName + ". Please set it with command: Change trade profile set maximum stops, followed by number of stops."));
            return;
        }


        if (profileManager.getCriteria().getMaxHopDistance() == 0){
            String shipName = playerSession.getShipLoadout().getShipName();
            EventBusManager.publish(new AiVoxResponseEvent("There is no maximum distance from entry set for " + shipName + ". Please set it with command: Change trade profile set distance from entry, followed by a number of light seconds."));
            return;
        }

        TradeRouteResponse route = tradeRouteManager.calculateTradeRoute();
        long totalProfit = route.getResult().getLast().getTotalProfit();
        EventBusManager.publish(new AiVoxResponseEvent("Calculated route with profit of " + totalProfit + " credits. Ask me to plot the route to next trade station."));
    }
}

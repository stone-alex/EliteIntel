package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.db.managers.TradeRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.traderoute.TradeRouteResponse;
import elite.intel.search.spansh.traderoute.TradeRouteSearchCriteria;
import elite.intel.search.spansh.traderoute.TradeRouteTransaction;
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

        EventBusManager.publish(new AiVoxResponseEvent("Calculating trade route. Please wait. This takes time."));

        TradeRouteSearchCriteria criteria = profileManager.getCriteria(true);

        if (criteria == null) {
            return;
        }

        if (criteria.getStartingCapital() == 0) {
            String shipName = playerSession.getShipLoadout().getShipName();
            StringBuilder sb = new StringBuilder();
            sb.append(" There is no trading profile for " + shipName + " You will have to set one up once per cargo ship you own.");
            sb.append(" To set a trading profile, say: Change trading profile, followed by profile parameter.");
            sb.append(" Required parameters are -");
            sb.append(" Starting Capital, Distance from entry, Maximum stops.");
            sb.append(" Optional parameters are - Allow Permit protected systems, Allow Planetary Ports, Allow Fleet Carriers and Allow Prohibited cargo.");
            sb.append(" Example: change trading profile, set Starting Capital 100000.");
            sb.append(" Please set one parameter at a time. Ask me to list trade profile parameters if you need help.");
            EventBusManager.publish(
                    new MissionCriticalAnnouncementEvent(sb.toString())
            );
            return;
        }

        if (criteria.getMaxJumps() == 0) {
            String shipName = playerSession.getShipLoadout().getShipName();
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("There is no number of stops set for " + shipName + ". Please set it with command: Change trade profile set maximum stops, followed by number of stops."));
            return;
        }


        if (criteria.getMaxLsFromArrival() == 0) {
            String shipName = playerSession.getShipLoadout().getShipName();
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("There is no maximum distance from star to port set for " + shipName + ". Please set it with command: Change trade profile set distance from entry, followed by a number of light seconds."));
            return;
        }

        TradeRouteResponse route = tradeRouteManager.calculateTradeRoute(criteria);
        if (route == null || route.getResult() == null || route.getResult().isEmpty()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No trade route found."));
            return;
        }
        long totalProfit = route.getResult().stream()
                .mapToLong(TradeRouteTransaction::getTotalProfit)
                .sum();

        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Calculated route with profit of " + totalProfit + " credits. Ask me to plot the route to next trade station."));
    }
}

package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.TradeProfileManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.edsm.commodity.CommoditySearchResult;
import elite.intel.search.edsm.commodity.EdsmCommoditySearch;
import elite.intel.search.spansh.traderoute.TradeRouteSearchCriteria;
import elite.intel.session.PlayerSession;

import java.util.List;

import static elite.intel.util.StringUtls.capitalizeWords;
import static elite.intel.util.StringUtls.getIntSafely;

public class FindCommodityHandler implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final TradeProfileManager tradeProfileManager = TradeProfileManager.getInstance();


    @Override public void handle(String action, JsonObject params, String responseText) {

        JsonElement key = params.get("key");
        JsonElement maxGalacticDistance = params.get("max_distance");
        JsonElement stateEl = params.get("state");
        boolean returnClosest = stateEl != null && stateEl.getAsBoolean();
        Integer distance = maxGalacticDistance == null ? null : getIntSafely(maxGalacticDistance.getAsString());
        if (distance == null || distance < 1) distance = (int) playerSession.getShipLoadout().getMaxJumpRange() * 2;
        String starName = playerSession.getPrimaryStarName();

        if (key == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Please specify a commodity."));
            return;
        }

        String commodity =
                capitalizeWords(
                        FuzzySearch.fuzzyCommodityMatch(
                                key.getAsString(), 3
                        )
                );

        if (commodity == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Sorry, I couldn't find any commodities matching " + key.getAsString()));
            return;
        }

        String searchMode = returnClosest ? "nearest market" : "best price";
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Searching for " + searchMode + " for " + commodity + " within " + distance + " light years."));
        TradeRouteSearchCriteria tradeProfileManagerCriteria = tradeProfileManager.getCriteria(false);
        int cargoCapacity = tradeProfileManagerCriteria.getMaxCargo();
        int maxDistanceFromArrival = tradeProfileManagerCriteria.getMaxLsFromArrival();
        List<CommoditySearchResult> results = EdsmCommoditySearch.search(
                commodity,
                starName,
                distance,
                maxDistanceFromArrival,
                cargoCapacity,
                returnClosest
        );
        if (results.isEmpty()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No match found."));
            return;
        }
        ReminderManager reminderManager = ReminderManager.getInstance();
        CommoditySearchResult result = results.getFirst();
        String reminder = "Head to " + result.getStarSystem() + " star system, " + result.getStationName() + " " + result.getStationType() + ". Price per unit is " + result.getPrice() + " credits.";
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(reminder));
        reminderManager.setReminder(
                reminder,
                result.getStarSystem()
        );

        RoutePlotter plotter = new RoutePlotter();
        plotter.plotRoute(result.getStarSystem());
    }
}

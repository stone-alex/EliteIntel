package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.FuzzySearch;
import elite.intel.db.managers.ReminderManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.edsm.commodity.CommoditySearchResult;
import elite.intel.search.edsm.commodity.EdsmCommoditySearch;
import elite.intel.session.PlayerSession;

import java.util.List;

import static elite.intel.util.StringUtls.capitalizeWords;
import static elite.intel.util.StringUtls.getIntSafely;

public class FindCommodityHandler extends CommandOperator implements CommandHandler {

    private final GameController controller;
    private final PlayerSession playerSession = PlayerSession.getInstance();

    public FindCommodityHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.controller = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        JsonElement key = params.get("key");
        JsonElement maxDistance = params.get("max_distance");
        Integer distance = maxDistance == null ? null : getIntSafely(maxDistance.getAsString());
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

        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Searching markets with best price for " + commodity + " within " + distance + " light years."));

        List<CommoditySearchResult> results = EdsmCommoditySearch.search(commodity, starName, distance);
        if (results.isEmpty()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No match found."));
            return;
        }
        ReminderManager reminderManager = ReminderManager.getInstance();
        CommoditySearchResult result = results.getFirst();
        String reminder = "Head to " + result.getStarSystem() + " star system, " + result.getStationName() + " " + result.getStationType() + ". Price per unit is " + result.getPrice() + " credits.";
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(reminder));
        reminderManager.setReminder(reminder);

        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(result.getStarSystem());
    }
}

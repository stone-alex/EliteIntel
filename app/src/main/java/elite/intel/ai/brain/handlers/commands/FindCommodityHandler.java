package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.edsm.commodity.CommoditySearchResult;
import elite.intel.search.edsm.commodity.EdsmCommoditySearch;
import elite.intel.session.PlayerSession;

import java.util.List;

import static elite.intel.util.StringUtls.*;

public class FindCommodityHandler extends CommandOperator implements CommandHandler {

    private GameController controller;
    private PlayerSession playerSession = PlayerSession.getInstance();

    public FindCommodityHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
        this.controller = commandHandler;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        JsonElement key = params.get("key");
        Integer distance = getIntSafely(params.get("max_distance").getAsString());
        if (distance < 1) distance = (int) playerSession.getShipLoadout().getMaxJumpRange() * 2;
        String starName = playerSession.getPrimaryStarName();

        if (key == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Please specify a commodity."));
            return;
        }

        if (distance == null) {
            distance = (int) (playerSession.getShipLoadout().getMaxJumpRange() * 5);
        }

        String commodity =
                capitalizeWords(
                        fuzzyCommodityMatch(
                                key.getAsString(), 3
                        )
                );

        if (commodity == null) {
            EventBusManager.publish(new AiVoxResponseEvent("Sorry, I couldn't find any commodities matching " + key.getAsString()));
            return;
        }

        EventBusManager.publish(new AiVoxResponseEvent("Searching markets with best price for " + commodity + " within " + distance + " light years."));

        List<CommoditySearchResult> results = EdsmCommoditySearch.search(commodity, starName, distance);
        if (results.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No commodities found."));
            return;
        }
        DestinationReminderManager reminderManager = DestinationReminderManager.getInstance();
        CommoditySearchResult result = results.getFirst();
        EventBusManager.publish(new AiVoxResponseEvent("Head to " + result.getStarSystem() + " star system, " + result.getStationName() + " " + result.getStationType() + ". Price per unit is " + result.getPrice() + " credits."));
        reminderManager.setDestination(result.toJson());
        RoutePlotter plotter = new RoutePlotter(this.controller);
        plotter.plotRoute(result.getStarSystem());
    }
}

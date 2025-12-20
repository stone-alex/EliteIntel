package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.MonetizeRouteManager;
import elite.intel.db.managers.ShipManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.edsm.monetize.MonetizeRoute;

public class MonetizeRouteHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        ShipManager shipManager = ShipManager.getInstance();
        if (shipManager.getShip().getCargoCapacity() < 1) {
            EventBusManager.publish(new AiVoxResponseEvent("Ship does not have enough cargo capacity."));
            return;
        }

        MonetizeRouteManager monetizeRouteManager = MonetizeRouteManager.getInstance();
        MonetizeRoute.TradeTuple tradeTuple = monetizeRouteManager.monetizeRoute();

        if (tradeTuple == null) {
            EventBusManager.publish(new AiVoxResponseEvent("No trade found."));
        } else {
            EventBusManager.publish(
                    new AiVoxResponseEvent(
                            "Trade found. Stop at " + tradeTuple.getSource().getStarSystem() + " star system. " + tradeTuple.getSource().getStationName() + ". Pick up " + tradeTuple.getSource().getCommodity() +
                                    " and deliver to " + tradeTuple.getDestination().getStarSystem() + " star system, " + tradeTuple.getDestination().getStationName() + " port. "
                    )
            );
        }
    }
}

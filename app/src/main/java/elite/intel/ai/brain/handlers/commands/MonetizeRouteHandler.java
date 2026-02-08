package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.MonetizeRouteManager;
import elite.intel.db.managers.ReminderManager;
import elite.intel.db.managers.ShipManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.edsm.monetize.MonetizeRoute;

public class MonetizeRouteHandler implements CommandHandler {

    private final MonetizeRouteManager monetizeRouteManager = MonetizeRouteManager.getInstance();
    private final ReminderManager reminderManager = ReminderManager.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        ShipManager shipManager = ShipManager.getInstance();
        if (shipManager.getShip().getCargoCapacity() < 1) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Ship does not have enough cargo capacity."));
            return;
        }
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Searching markets along our route. This might take some time..."));

        MonetizeRoute.TradeTransaction tradeTuple = monetizeRouteManager.monetizeRoute();

        if (tradeTuple == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No trade found."));
        } else {
            String reminder = "Stop at " + tradeTuple.getSource().getStarSystem() + " star system. "+ tradeTuple.getSource().getStationName()
                    + ". Pick up " + tradeTuple.getSource().getCommodity() +
                    " and deliver to " + tradeTuple.getDestination().getStarSystem()
                    + " star system, "
                    + tradeTuple.getDestination().getStationName() + " port. ";

            reminderManager.setReminder(reminder);

            EventBusManager.publish(
                    new MissionCriticalAnnouncementEvent(
                            "Trade found. " + reminder
                    )
            );
        }
    }
}

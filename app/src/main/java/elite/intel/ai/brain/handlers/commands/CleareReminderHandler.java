package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.db.managers.MonetizeRouteManager;
import elite.intel.gameapi.EventBusManager;

public class CleareReminderHandler implements CommandHandler {

    private final DestinationReminderManager destinationReminder = DestinationReminderManager.getInstance();
    private final MonetizeRouteManager monetizeRouteManager = MonetizeRouteManager.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        destinationReminder.clear();
        monetizeRouteManager.clear();
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Reminders cleared"));
    }
}

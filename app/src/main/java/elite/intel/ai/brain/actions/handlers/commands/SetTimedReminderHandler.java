package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.TimedReminderManager;
import elite.intel.gameapi.EventBusManager;

public class SetTimedReminderHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        JsonElement keyEl = params.get("key");
        JsonElement minutesEl = params.get("minutes");

        if (keyEl == null || minutesEl == null) {
            EventBusManager.publish(new AiVoxResponseEvent("reminder text and duration required"));
            return;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(minutesEl.getAsString().trim());
        } catch (NumberFormatException e) {
            EventBusManager.publish(new AiVoxResponseEvent("invalid duration for timed reminder"));
            return;
        }

        if (minutes <= 0) {
            EventBusManager.publish(new AiVoxResponseEvent("duration must be greater than zero"));
            return;
        }

        String text = keyEl.getAsString();
        TimedReminderManager.getInstance().schedule(text, minutes);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(
                "Reminder set for " + minutes + " minute" + (minutes == 1 ? "" : "s")));
    }
}
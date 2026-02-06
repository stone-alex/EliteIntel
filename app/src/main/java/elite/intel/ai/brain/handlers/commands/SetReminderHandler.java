package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.ReminderManager;
import elite.intel.gameapi.EventBusManager;

public class SetReminderHandler implements CommandHandler {


    @Override public void handle(String action, JsonObject params, String responseText) {
        ReminderManager reminderManager = ReminderManager.getInstance();
        JsonElement key = params.get("key");
        if (key != null) {
            reminderManager.setDestination(key.getAsString());
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("no reminder key parameter provided"));
        }
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.db.managers.ReminderManager;


public class RemindTargetDestinationHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final ReminderManager destinationReminder = ReminderManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        String reminder = destinationReminder.getReminderText();
        return process(reminder.isBlank() ? "No reminder set" : reminder);
    }
}
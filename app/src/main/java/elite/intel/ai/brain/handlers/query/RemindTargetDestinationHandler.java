package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.db.dao.DestinationReminderDao;
import elite.intel.db.managers.ReminderManager;


public class RemindTargetDestinationHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final ReminderManager destinationReminder = ReminderManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        DestinationReminderDao.Reminder reminder = destinationReminder.getReminder();
        String reminderText = reminder.getReminder();
        return process(reminderText != null && reminderText.isBlank() ? "No reminder set" : reminderText);
    }
}
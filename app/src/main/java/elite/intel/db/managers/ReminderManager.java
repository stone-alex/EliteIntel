package elite.intel.db.managers;

import elite.intel.db.dao.DestinationReminderDao;
import elite.intel.db.util.Database;

public final class ReminderManager {
    private static volatile ReminderManager instance;

    private ReminderManager() {
    }

    public static ReminderManager getInstance() {
        ReminderManager result = instance;
        if (result == null) {
            synchronized (ReminderManager.class) {
                result = instance;
                if (result == null) {
                    instance = result = new ReminderManager();
                }
            }
        }
        return result;
    }


    public DestinationReminderDao.Reminder getReminder() {
        return Database.withDao(DestinationReminderDao.class, DestinationReminderDao::get);
    }

    public void setReminder(String text, String starSystem) {
        Database.withDao(DestinationReminderDao.class, dao -> {
            DestinationReminderDao.Reminder data = new DestinationReminderDao.Reminder();
            data.setStarSystem(starSystem);
            data.setReminder(text);
            dao.save(data);
            return null;
        });
    }

    public void clear() {
        Database.withDao(DestinationReminderDao.class, dao -> {
            dao.clear();
            return null;
        });
    }

}

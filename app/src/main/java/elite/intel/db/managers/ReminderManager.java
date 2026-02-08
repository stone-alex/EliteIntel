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


    public String getReminderText() {
        return Database.withDao(DestinationReminderDao.class, dao -> {
            DestinationReminderDao.Destination destination = dao.get();
            if (destination == null) return null;
            return destination.getJson();
        });
    }

    public void setReminder(String text) {
        Database.withDao(DestinationReminderDao.class, dao -> {
            DestinationReminderDao.Destination data = new DestinationReminderDao.Destination();
            data.setJson(text);
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

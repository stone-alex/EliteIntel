package elite.intel.db.managers;

import elite.intel.ai.search.spansh.station.DestinationDto;
import elite.intel.db.dao.DestinationReminderDao;
import elite.intel.db.util.Database;
import elite.intel.util.json.GsonFactory;

public final class DestinationReminderManager {
    private static volatile DestinationReminderManager instance;

    private DestinationReminderManager() {
    }

    public static DestinationReminderManager getInstance() {
        DestinationReminderManager result = instance;
        if (result == null) {
            synchronized (DestinationReminderManager.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DestinationReminderManager();
                }
            }
        }
        return result;
    }


    public DestinationDto getDestination() {
        return Database.withDao(DestinationReminderDao.class, dao -> {
            DestinationReminderDao.Destination destination = dao.get();
            if (destination == null) return null;
            return GsonFactory.getGson().fromJson(destination.getJson(), DestinationDto.class);
        });

    }

    public void setDestination(String json) {
        Database.withDao(DestinationReminderDao.class, dao -> {
            DestinationReminderDao.Destination data = new DestinationReminderDao.Destination();
            data.setJson(json);
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

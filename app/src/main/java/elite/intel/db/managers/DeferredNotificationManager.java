package elite.intel.db.managers;

import elite.intel.db.dao.DeferredNotificationDao;
import elite.intel.db.util.Database;
import elite.intel.util.Md5Utils;

import java.util.List;

public class DeferredNotificationManager {

    private static DeferredNotificationManager INSTANCE;


    private DeferredNotificationManager() {
    }

    public static synchronized DeferredNotificationManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DeferredNotificationManager();
        }
        return INSTANCE;
    }


    public void scheduleNotification(String notification, Long delay) {
        Database.withDao(DeferredNotificationDao.class, dao -> {
            DeferredNotificationDao.DeferredNotification entity = new DeferredNotificationDao.DeferredNotification();
            entity.setNotification(notification);
            entity.setTimeToNotify(System.currentTimeMillis() + delay);
            entity.setKey(Md5Utils.generateMd5(notification));
            dao.insert(entity);

            return Void.class;
        });
    }

    public List<DeferredNotificationDao.DeferredNotification> get() {
        return Database.withDao(DeferredNotificationDao.class, dao -> dao.get(System.currentTimeMillis()));
    }

    public void delete(DeferredNotificationDao.DeferredNotification notification) {
        Database.withDao(DeferredNotificationDao.class, dao -> {
                    dao.delete(notification.getKey());
                    return Void.class;
                }
        );
    }
}

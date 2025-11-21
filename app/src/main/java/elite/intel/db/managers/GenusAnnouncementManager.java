package elite.intel.db.managers;

import elite.intel.db.dao.GenusPaymentAnnouncementDao;
import elite.intel.db.util.Database;

public class GenusAnnouncementManager {
    private static GenusAnnouncementManager instance;

    private GenusAnnouncementManager() {
    }

    public static synchronized GenusAnnouncementManager getInstance() {
        if (instance == null) {
            instance = new GenusAnnouncementManager();
        }
        return instance;
    }

    public void put(String genus, boolean isOn) {
        Database.withDao(GenusPaymentAnnouncementDao.class, dao -> {;
            dao.upsert(genus, isOn);
            return null;
        });

    }

    public void clear() {
        Database.withDao(GenusPaymentAnnouncementDao.class, dao ->{
            dao.clear();
            return null;
        });
    }

    public Boolean get(String genus) {
        return Database.withDao(GenusPaymentAnnouncementDao.class, dao ->{
            GenusPaymentAnnouncementDao.GenusPaymentAnouncement data = dao.get(genus);
            return data == null ? false : data.getOn();
        });
    }
}

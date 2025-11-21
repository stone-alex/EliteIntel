package elite.intel.db;

import elite.intel.db.dao.ShipScansDao;
import elite.intel.db.util.Database;

public class ShipScans {
    private static ShipScans instance;

    private ShipScans() {
    }

    public static synchronized ShipScans getInstance() {
        if (instance == null) {
            instance = new ShipScans();
        }
        return instance;
    }


    public void saveScan(ShipScansDao.ShipScan data) {
        Database.withDao(ShipScansDao.class, dao -> {
            dao.upsert(data);
            return null;
        });
    }


    public String get(String key) {
        return Database.withDao(ShipScansDao.class, dao -> dao.get(key));
    }

    public void clear() {
        Database.withDao(ShipScansDao.class, dao ->{
            dao.clear();
            return null;
        });
    }
}

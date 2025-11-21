package elite.intel.db.managers;

import elite.intel.db.dao.TargetLocationDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.dto.TargetLocation;
import elite.intel.util.json.GsonFactory;

public class TargetLocationManager {
    private static TargetLocationManager instance;

    private TargetLocationManager() {
    }

    public static synchronized TargetLocationManager getInstance() {
        if (instance == null) {
            instance = new TargetLocationManager();
        }
        return instance;
    }

    public TargetLocation get() {
        return Database.withDao(TargetLocationDao.class, dao -> {
            TargetLocationDao.TargetLocation location = dao.get();
            if(location == null) return null;
            return GsonFactory.getGson().fromJson(location.getJson(), TargetLocation.class);
        });
    }

    public void save(TargetLocation tracking) {
        Database.withDao(TargetLocationDao.class, dao ->{
            TargetLocationDao.TargetLocation data = new TargetLocationDao.TargetLocation();
            data.setJson(tracking.toJson());
            dao.save(data);
            return null;
        });
    }
}

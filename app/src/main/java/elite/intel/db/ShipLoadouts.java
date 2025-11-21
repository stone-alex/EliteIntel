package elite.intel.db;

import elite.intel.db.dao.ShipLoadoutDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.util.json.GsonFactory;

public class ShipLoadouts {
    private static volatile ShipLoadouts instance;

    private ShipLoadouts() {
    }

    public static ShipLoadouts getInstance() {
        if (instance == null) {
            synchronized (ShipLoadouts.class) {
                if (instance == null) {
                    instance = new ShipLoadouts();
                }
            }
        }
        return instance;
    }

    public void save(LoadoutEvent event) {
        Database.withDao(ShipLoadoutDao.class, dao -> {
            ShipLoadoutDao.ShipLoadout data = new ShipLoadoutDao.ShipLoadout();
            data.setJson(event.toJson());
            dao.save(data);
            return null;
        });
    }

    public LoadoutEvent get() {
        return Database.withDao(ShipLoadoutDao.class, dao ->{
            ShipLoadoutDao.ShipLoadout shipLoadout = dao.get();
            return GsonFactory.getGson().fromJson(shipLoadout.getJson(), LoadoutEvent.class);
        });
    }

    public void clear() {
        Database.withDao(ShipLoadoutDao.class, dao ->{
            dao.clear();
            return null;
        });
    }
}

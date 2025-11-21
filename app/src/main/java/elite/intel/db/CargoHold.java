package elite.intel.db;

import elite.intel.db.dao.CargoDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.util.json.GsonFactory;

public class CargoHold {
    private static CargoHold instance;

    private CargoHold() {
    }

    public static synchronized CargoHold getInstance() {
        if (instance == null) {
            instance = new CargoHold();
        }
        return instance;
    }


    public void save(GameEvents.CargoEvent event) {
        Database.withDao(CargoDao.class, dao ->{
            CargoDao.Cargo data = new CargoDao.Cargo();
            data.setJson(event.toJson());
            dao.save(data);
            return null;
        });
    }

    public GameEvents.CargoEvent get() {
        return Database.withDao(CargoDao.class, dao -> {
            CargoDao.Cargo cargo = dao.get();
            if(cargo == null) return new GameEvents.CargoEvent();
            return GsonFactory.getGson().fromJson(cargo.getJson(), GameEvents.CargoEvent.class);
        });
    }
}

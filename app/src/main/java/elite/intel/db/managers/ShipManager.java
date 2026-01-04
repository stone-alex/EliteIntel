package elite.intel.db.managers;

import elite.intel.db.dao.ShipDao;
import elite.intel.db.util.Database;
import elite.intel.util.ShipPadSizes;

public class ShipManager {
    private static ShipManager instance;

    private ShipManager() {
    }

    public static synchronized ShipManager getInstance() {
        if (instance == null) {
            instance = new ShipManager();
        }
        return instance;
    }

    /**
     * Creates or updates ship on LoadoutEvent.
     *
     */
    public void save(int shipId, String shipName, int cargoCapacity, String shipMake) {
        Database.withDao(ShipDao.class, dao -> {
            ShipDao.Ship ship = new ShipDao.Ship();
            ship.setShipId(shipId);
            ship.setShipName(shipName);
            ship.setCargoCapacity(cargoCapacity);
            ship.setShipIdentifier(shipMake);
            dao.save(ship);
            return null;
        });
    }

    public ShipDao.Ship getShip() {
        return Database.withDao(ShipDao.class, dao -> dao.findShip(
                        ShipLoadoutManager.getInstance().get().getShipId()
                )
        );
    }

    public boolean requireLargePad() {
        return "L".equals(ShipPadSizes.getPadSize(getShip().getShipIdentifier()));
    }
}

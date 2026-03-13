package elite.intel.db.managers;

import elite.intel.db.dao.ShipDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.dto.shiploadout.ShipLoadOutDto;
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
            return Void.TYPE;
        });
    }

    public ShipDao.Ship getShip() {
        return Database.withDao(ShipDao.class, dao -> {
                    ShipLoadOutDto dto = ShipLoadoutManager.getInstance().get();
                    if (dto == null) return null;
                    return dao.findShip(dto.getShipId());
                }
        );
    }

    public boolean requireLargePad() {
        ShipDao.Ship ship = getShip();
        if (ship == null) return false;
        return "L".equals(ShipPadSizes.getPadSize(ship.getShipIdentifier()));
    }
}

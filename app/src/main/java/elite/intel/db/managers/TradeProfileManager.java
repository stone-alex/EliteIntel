package elite.intel.db.managers;

import elite.intel.db.dao.ShipDao;
import elite.intel.db.dao.TradeProfileDao;
import elite.intel.db.util.Database;
import elite.intel.search.spansh.traderoute.TradeRouteSearchCriteria;

public class TradeProfileManager {

    private static TradeProfileManager instance;
    private final ShipManager shipManager = ShipManager.getInstance();

    private TradeProfileManager() {
    }

    public static synchronized TradeProfileManager getInstance() {
        if (instance == null) {
            instance = new TradeProfileManager();
        }
        return instance;
    }

    public TradeRouteSearchCriteria getCriteria() {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return null;
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        TradeRouteSearchCriteria criteria = new TradeRouteSearchCriteria();
        criteria.setAllowPlanetary(profile.isAllowPlanetary());
        criteria.setAllowPermit(profile.isAllowPermit());
        criteria.setAllowProhibited(profile.isAllowProhibited());
        criteria.setAllowFleetCarriers(profile.isAllowFleetCarrier());
        criteria.setMaxCargo(ship.getCargoCapacity());
        criteria.setMaxHopDistance(profile.getMaxDistanceLs());
        criteria.setMaxHops(profile.getMaxJumps());
        criteria.setRequiresLargePad(true /* TODO: Need a way to determine this*/);
        criteria.setStartingCapital(profile.getStartingBudget());
        return criteria;
    }

    private TradeProfileDao.TradeProfile getProfile(ShipDao.Ship ship) {
        if (ship == null) return null;
        TradeProfileDao.TradeProfile profile = Database.withDao(TradeProfileDao.class, dao -> {
            TradeProfileDao.TradeProfile p = dao.getTradeProfile(ship.getShipId());
            if (p == null) {
                p = new TradeProfileDao.TradeProfile();
                p.setShipId(ship.getShipId());// unique id for this profile
                dao.save(p);
            }
            return p;
        });
        return profile;
    }

    public void setStartingCapitol(Integer startingCapital) {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return;
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        profile.setStartingBudget(startingCapital);
        Database.withDao(TradeProfileDao.class, dao -> {
            dao.save(profile);
            return Void.class;
        });
    }

    public void setDistanceFromSystemEntry(Integer distance) {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return;
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        profile.setMaxDistanceLs(distance);
        Database.withDao(TradeProfileDao.class, dao -> {
            dao.save(profile);
            return Void.class;
        });
    }


    public void setMaximumStops(Integer maxStops) {
        final ShipDao.Ship ship = shipManager.getShip();
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        profile.setMaxJumps(maxStops);
        Database.withDao(TradeProfileDao.class, dao -> {
            dao.save(profile);
            return Void.class;
        });
    }

    public void setAllowFleetCarrier(boolean allowFleetCarrier) {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return;
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        profile.setAllowFleetCarrier(allowFleetCarrier);
        Database.withDao(TradeProfileDao.class, dao -> {
            dao.save(profile);
            return Void.class;
        });
    }

    public void setAllowPermit(boolean allowPermit) {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return;
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        profile.setAllowPermit(allowPermit);
        Database.withDao(TradeProfileDao.class, dao -> {
            dao.save(profile);
            return Void.class;
        });
    }

    public void setAllowProhibitedCargo(boolean allowProhibitedCargo) {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return;
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        profile.setAllowProhibited(allowProhibitedCargo);
        Database.withDao(TradeProfileDao.class, dao -> {
            dao.save(profile);
            return Void.class;
        });
    }


    public void setAllowPlanetaryPorts(boolean allowPlanetaryPorts) {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return;
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        profile.setAllowPlanetary(allowPlanetaryPorts);
        Database.withDao(TradeProfileDao.class, dao -> {
            dao.save(profile);
            return Void.class;
        });
    }

    public void setAllowSystemPermits(boolean allowSystemPermits) {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return;
        TradeProfileDao.TradeProfile profile = getProfile(ship);
        profile.setAllowPermit(allowSystemPermits);
        Database.withDao(TradeProfileDao.class, dao -> {
            dao.save(profile);
            return Void.class;
        });
    }

    public boolean hasCargoCapacity() {
        return shipManager.getShip().getCargoCapacity() > 0;
    }
}

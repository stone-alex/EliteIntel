package elite.intel.db.managers;

import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.db.dao.FleetCarrierRouteDao;
import elite.intel.db.util.Database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FleetCarrierRouteManager {

    private static volatile FleetCarrierRouteManager instance;

    private FleetCarrierRouteManager() {
        // enforce singleton pattern.
    }

    public static FleetCarrierRouteManager getInstance() {
        if (instance == null) {
            synchronized (FleetCarrierRouteManager.class) {
                if (instance == null) {
                    instance = new FleetCarrierRouteManager();
                }
            }
        }
        return instance;
    }


    public void setFleetCarrierRoute(Map<Integer, CarrierJump> fleetCarrierRoute) {
        clear();
        Database.withDao(FleetCarrierRouteDao.class, dao -> {

            Set<Integer> keys = fleetCarrierRoute.keySet();
            for (Integer key : keys) {
                FleetCarrierRouteDao.FleetCarrierRouteLeg leg = new FleetCarrierRouteDao.FleetCarrierRouteLeg();
                leg.setLeg(key);
                leg.setDistance(fleetCarrierRoute.get(key).getDistance());
                leg.setFuelUsed(fleetCarrierRoute.get(key).getFuelUsed());
                leg.setHasIcyRing(fleetCarrierRoute.get(key).getHasIcyRing());
                leg.setPristine(fleetCarrierRoute.get(key).isPristine());
                leg.setRemainingFuel(fleetCarrierRoute.get(key).getRemainingFuel());
                leg.setSystemName(fleetCarrierRoute.get(key).getSystemName());
                leg.setX(fleetCarrierRoute.get(key).getX());
                leg.setY(fleetCarrierRoute.get(key).getY());
                leg.setZ(fleetCarrierRoute.get(key).getZ());
                dao.save(leg);
            }
            return null;
        });

    }

    public Map<Integer, CarrierJump> getFleetCarrierRoute() {
        return Database.withDao(FleetCarrierRouteDao.class, dao -> {
            Map<Integer, CarrierJump> result = new HashMap<>();
            List<FleetCarrierRouteDao.FleetCarrierRouteLeg> all = dao.getAll();
            for (FleetCarrierRouteDao.FleetCarrierRouteLeg leg : all) {
                CarrierJump jump = entityToDto(leg);
                result.put(leg.getLeg(), jump);
            }
            return result;
        });
    }

    private static CarrierJump entityToDto(FleetCarrierRouteDao.FleetCarrierRouteLeg leg) {
        CarrierJump jump = new CarrierJump();
        if(leg == null) return jump;
        jump.setLeg(leg.getLeg());
        jump.setDistance(leg.getDistance());
        jump.setFuelUsed(leg.getFuelUsed());
        jump.setHasIcyRing(leg.getHasIcyRing());
        jump.setPristine(leg.getPristine());
        jump.setRemainingFuel(leg.getRemainingFuel());
        jump.setSystemName(leg.getSystemName());
        jump.setX(leg.getX());
        jump.setY(leg.getY());
        jump.setZ(leg.getZ());
        return jump;
    }

    public void removeLeg(String starName) {
        Database.withDao(FleetCarrierRouteDao.class, dao -> {
                    dao.delete(starName);
                    return null;
                }
        );
    }

    public void clear() {
        Database.withDao(FleetCarrierRouteDao.class, dao -> {
            dao.clear();
            return null;
        });
    }

    public CarrierJump findByPrimaryStar(String starSystem) {
        return Database.withDao(FleetCarrierRouteDao.class, dao ->{
            FleetCarrierRouteDao.FleetCarrierRouteLeg leg = dao.findByPrimaryStarName(starSystem);
            return entityToDto(leg);
        });
    }
}

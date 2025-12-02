package elite.intel.db.managers;

import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.dao.ShipDao;
import elite.intel.db.dao.TradeProfileDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.station.StationSearchClient;
import elite.intel.search.spansh.station.marketstation.TradeStationSearchCriteria;
import elite.intel.search.spansh.station.marketstation.TradeStationSearchResultDto;
import elite.intel.search.spansh.traderoute.TradeRouteSearchCriteria;
import elite.intel.session.PlayerSession;
import elite.intel.util.NavigationUtils;
import elite.intel.util.ShipPadSizes;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TradeProfileManager {

    public static final int MAX_DISTANCE_TO_STATION = 100;
    private static final Logger log = LogManager.getLogger(TradeProfileManager.class);
    private static TradeProfileManager instance;
    private final ShipManager shipManager = ShipManager.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private TradeProfileManager() {
    }

    public static synchronized TradeProfileManager getInstance() {
        if (instance == null) {
            instance = new TradeProfileManager();
        }
        return instance;
    }

    public TradeRouteSearchCriteria getCriteria(boolean withStationStartingStation) {
        final ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return null;

        TradeProfileDao.TradeProfile profile = getProfile(ship);
        TradeRouteSearchCriteria criteria = new TradeRouteSearchCriteria();
        criteria.setAllowPlanetary(profile.isAllowPlanetary());
        criteria.setAllowPermit(profile.isAllowPermit());
        criteria.setAllowProhibited(profile.isAllowProhibited());
        criteria.setAllowFleetCarriers(profile.isAllowFleetCarrier());
        criteria.setMaxCargo(ship.getCargoCapacity());
        criteria.setMaxJumpDistance((int) playerSession.getShipLoadout().getMaxJumpRange());
        criteria.setMaxSystemDistance(profile.getMaxDistanceLs());
        criteria.setMaxJumps(profile.getMaxJumps());
        criteria.setRequiresLargePad("L".equals(ShipPadSizes.getPadSize(ship.getShipIdentifier())));
        criteria.setStartingCapital(profile.getStartingBudget());

        if (withStationStartingStation) {
            StationSearchClient stationSearchClient = StationSearchClient.getInstance();

            TradeStationSearchCriteria initialStationCriteria = new TradeStationSearchCriteria();
            TradeStationSearchCriteria.ReferenceCoords coords = new TradeStationSearchCriteria.ReferenceCoords();
            LocationManager locationManager = LocationManager.getInstance();
            LocationDao.Coordinates galacticCoordinates = locationManager.getGalacticCoordinates();

            if (galacticCoordinates == null) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Galactic coordinates are not available."));
                return null;
            }
            if (galacticCoordinates.x() == 0 && galacticCoordinates.y() == 0 && galacticCoordinates.z() == 0) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Galactic coordinates are not available."));
                return null;
            }

            coords.setX(galacticCoordinates.x());
            coords.setY(galacticCoordinates.y());
            coords.setZ(galacticCoordinates.z());
            initialStationCriteria.setReferenceCoords(coords);
            initialStationCriteria.setPage(0);
            initialStationCriteria.setSize(1);

            TradeStationSearchCriteria.Filters filters = new TradeStationSearchCriteria.Filters();
            filters.setDistanceToArrival(new TradeStationSearchCriteria.RangeFilter(0, 6000));
            initialStationCriteria.setFilters(filters);

            /// NOTE: Spansh API is very inconsistent. We can't reuse RangeFilter because distance must be passed without "<=>"
            TradeStationSearchCriteria.Distance distance = new TradeStationSearchCriteria.Distance();
            distance.setMax(250);
            distance.setMin(0);
            filters.setDistanceToStarSystem(distance);

            initialStationCriteria.setFilters(filters);
            log.debug("Initial station criteria: {}", initialStationCriteria.toJson());

            TradeStationSearchResultDto startingStation = GsonFactory.getGson().fromJson(
                    stationSearchClient.performSearch(initialStationCriteria), TradeStationSearchResultDto.class
            );

            // only one station should be returned if the station is null - the method will return false.
            if (startingStation.getResults().stream().anyMatch(this::isTooFar)) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Nearest trade station is too far away to calculate a trade route."));
                return null;
            }

            criteria.setStation(startingStation.getResults().stream().findFirst().get().getName());
            criteria.setSystem(startingStation.getResults().stream().findFirst().get().getSystemName());

            if (criteria.getStation() == null || criteria.getSystem() == null) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Unable to find a suitable trade station."));
                return null;
            } else {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Trade station found: " + criteria.getSystem() + " " + criteria.getStation() + ". Calculating trade route. This will take some time..."));
            }
        }
        log.debug("Trade route criteria: {}", criteria.toString());
        return criteria;
    }

    private boolean isTooFar(TradeStationSearchResultDto.StationResult stationResult) {
        if (stationResult == null) return false;
        LocationDao.Coordinates myLocation = LocationManager.getInstance().getGalacticCoordinates();
        double distance = NavigationUtils.calculateGalacticDistance(
                stationResult.getSystemX(), stationResult.getSystemY(), stationResult.getSystemZ(),
                myLocation.x(), myLocation.y(), myLocation.z()
        );

        boolean isToFar = MAX_DISTANCE_TO_STATION < distance;
        if (isToFar) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Found trade station within " + (int) distance + " light years."));
        }
        return isToFar;
    }

    private TradeProfileDao.TradeProfile getProfile(ShipDao.Ship ship) {
        if (ship == null) return null;
        TradeProfileDao.TradeProfile profile = Database.withDao(TradeProfileDao.class, dao -> {
            TradeProfileDao.TradeProfile p = dao.getTradeProfile(ship.getShipId());
            if (p == null) {
                p = new TradeProfileDao.TradeProfile();
                p.setShipId(ship.getShipId());// unique id for this profile
                p.setPadSize(ShipPadSizes.getPadSize(ship.getShipIdentifier()));
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
        return shipManager.getShip() == null ? false : shipManager.getShip().getCargoCapacity() > 0;
    }
}

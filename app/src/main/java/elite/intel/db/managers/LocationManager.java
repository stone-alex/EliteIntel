package elite.intel.db.managers;

import elite.intel.db.dao.LocationDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.util.json.GsonFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class for managing locations.
 * Use getInstance() to access the single instance of this class.
 */
public class LocationManager {
    private static LocationManager instance;

    private LocationManager() {
    }

    public static synchronized LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    public void save(LocationDto location) {
        if(location.getStarName() == null) return;
        Database.withDao(LocationDao.class, dao -> {
            String locationName = location.getPlanetName() == null ? location.getStationName() : location.getPlanetName();
            dao.upsert(location.getBodyId(), locationName, location.getStarName(), location.isHomeSystem(), location.toJson());
            return null;
        });
    }

    public void setAsHomeSystem() {
        Database.withDao(LocationDao.class, dao -> {
            dao.setCurrentStarSystemAsHome();
            return null;
        });
    }

    public LocationDao.Coordinates getGalacticCoordinates() {
        return Database.withDao(LocationDao.class, dao -> dao.currentCoordinates());
    }


    public LocationDto getLocation(String primaryStar, Long locationId) {
        return Database.withDao(LocationDao.class, dao -> {
            LocationDao.Location entity = dao.findByInGameIdAndPrimaryStar(locationId, primaryStar);
            if (entity == null) {
                return new LocationDto(locationId, primaryStar);
            }
            return GsonFactory.getGson().fromJson(entity.getJson(), LocationDto.class);
        });
    }

    public Map<Long, LocationDto> findByPrimaryStar(String primaryStar) {
        return Database.withDao(LocationDao.class, dao -> {
            List<LocationDao.Location> byPrimaryStar = dao.findByPrimaryStar(primaryStar);
            Map<Long, LocationDto> result = new HashMap<>();
            for (LocationDao.Location entity : byPrimaryStar) {
                result.put(entity.getInGameId(), GsonFactory.getGson().fromJson(entity.getJson(), LocationDto.class));
            }
            return result;
        });
    }

    public LocationDto findPrimaryStar(String starSystem) {
        return Database.withDao(LocationDao.class, dao -> {
            LocationDao.Location primaryStar = dao.findPrimaryStar(starSystem);
            return primaryStar == null ? new LocationDto(-1L) : GsonFactory.getGson().fromJson(primaryStar.getJson(), LocationDto.class);
        });
    }

    public LocationDto getHomeSystem() {
        return Database.withDao(LocationDao.class, dao ->{
            LocationDao.Location homeSystem = dao.findHomeSystem();
            return homeSystem == null ? new LocationDto(-1L) : GsonFactory.getGson().fromJson(homeSystem.getJson(), LocationDto.class);
        });
    }
}

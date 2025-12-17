package elite.intel.search.intra;

import elite.intel.db.dao.LocationDao;
import elite.intel.db.dao.PirateFactionDao.PirateFaction;
import elite.intel.db.dao.PirateMissionProviderDao.MissionProvider;
import elite.intel.db.managers.LocationManager;
import elite.intel.db.managers.PirateMissionDataManager;
import elite.intel.db.managers.PirateMissionDataManager.PirateMissionTuple;
import elite.intel.search.spansh.starsystems.StarSystemClient;
import elite.intel.search.spansh.starsystems.StarSystemResult;
import elite.intel.search.spansh.starsystems.SystemSearchCriteria;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.search.edsm.utils.StrongHoldFilter.skipEnemyStrongHold;

public class PirateMassacreMissionSearch {

    private static volatile PirateMassacreMissionSearch instance;
    private StarSystemClient starSystemSearchClient = StarSystemClient.getInstance();
    private IntraClient missionSearchClient = IntraClient.getInstance();
    private LocationManager locationManager = LocationManager.getInstance();
    private PlayerSession playerSession = PlayerSession.getInstance();
    private PirateMissionDataManager missionDataManager = PirateMissionDataManager.getInstance();

    private PirateMassacreMissionSearch() {
    }

    public static PirateMassacreMissionSearch getInstance() {
        if (instance == null) {
            synchronized (PirateMassacreMissionSearch.class) {
                if (instance == null) instance = new PirateMassacreMissionSearch();
            }
        }
        return instance;
    }


    /**
     * Finds a list of hunting spots in range for pirate missions based on the given distance and reconnaissance mode.
     * The method uses the player's current galactic coordinates to search for missions within a specified range.
     * If no missions are found, it attempts to query an external pirate massacre mission API for additional results.
     *
     * @param range the maximum distance from the player's current coordinates to look for hunting spots
     * @param recon a flag indicating whether to filter missions specifically for reconnaissance purposes
     * @return a list of {@code PirateMissionTuple} objects, where each tuple contains a {@code PirateFaction}
     *         representing the target faction and a list of {@code MissionProvider}s offering relevant missions
     */
    public List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> findHuntingSpotsInRange(int range, boolean recon) {
        LocationDao.Coordinates coordinates = locationManager.getGalacticCoordinates();
        List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> missions = new ArrayList<>();

        if (recon) {
            missions.addAll(missionDataManager.findInRangeForRecon(coordinates, range));
        } else {
            missions.addAll(missionDataManager.findInRange(coordinates, range));
        }

        if (missions.isEmpty()) {
            missions = PirateMassacreMissionSearch.getInstance().queryApi(coordinates, 50);
        }
        return missions;
    }


    /**
     * Queries an API to retrieve a list of pirate mission tuples based on the provided coordinates and range.
     * Filters and processes the data from the API to prepare mission pairs involving pirate factions
     * and their respective mission providers.
     *
     * @param coordinates the spatial coordinates in the galaxy used to locate potential pirate missions
     * @param range       the distance within which to search for pirate missions around the given coordinates
     * @return a list of pirate mission tuples, where each tuple contains a target pirate faction
     * and a list of corresponding mission providers; may return null if no valid data is retrieved
     */
    private List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> queryApi(LocationDao.Coordinates coordinates, int range) {
        String pledgedPower = playerSession.getRankAndProgressDto().getPledgedToPower();
        boolean pledgedToPower = (pledgedPower != null && !pledgedPower.isEmpty());

        IntraRequest request = new IntraRequest()
                .withResultLimit(40)
                .withMissionProviderMinFactions(1)
                .withArenaHazResDistanceFromEntryLs(50000)
                .withOurLocation(coordinates.x(), coordinates.y(), coordinates.z(), range);

        IntraResponse response = missionSearchClient.findMassacrePairs(request);
        if (response == null) return null;
        if (response.getBody() == null) return null;
        if (response.getBody().isEmpty()) return null;

        List<IntraResponse.Pair> responseBody = response.getBody();
        List<PirateMissionTuple<PirateFaction, List<MissionProvider>>> result = new ArrayList<>();

        for (IntraResponse.Pair pair : responseBody) {

            if (pair.getBettleGround() == null) continue;
            if (pair.getBettleGround().getHazRes() == null) continue;
            if (pair.getBettleGround().getHazRes().getConfirmed() < 1) continue;

            StarSystemResult missionProviderSystem = searchSystem(pair.getMissionProvider().getStarSystemName());
            StarSystemResult battleGroundSystem = searchSystem(pair.getBettleGround().getStarSystemName());

            if (pledgedToPower && skipEnemyStrongHold(missionProviderSystem, battleGroundSystem, pledgedPower)) continue;

            result.add(
                    missionDataManager.savePartialPair(
                            missionProviderSystem.getRecord(), battleGroundSystem.getRecord()
                    )
            );
        }

        return result;
    }


    private StarSystemResult searchSystem(String starSystemName) {
        SystemSearchCriteria criteria = new SystemSearchCriteria();
        SystemSearchCriteria.Filters filters = new SystemSearchCriteria.Filters();
        SystemSearchCriteria.SystemNameFilter systemName = new SystemSearchCriteria.SystemNameFilter();
        systemName.setValue(starSystemName);
        filters.setSystemName(systemName);
        criteria.setFilters(filters);
        criteria.setPage(1);
        criteria.setSize(10);
        return starSystemSearchClient.search(criteria);
    }
}

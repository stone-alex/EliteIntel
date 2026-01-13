package elite.intel.session;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.dao.PlayerDao;
import elite.intel.db.dao.ShipScansDao;
import elite.intel.db.managers.*;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.FsdTarget;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.journal.events.CarrierStatsEvent;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.gameapi.journal.events.ReputationEvent;
import elite.intel.gameapi.journal.events.dto.*;
import elite.intel.util.OsDetector;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Represents a player's session encompassing various aspects of gameplay including
 * missions, bounties, mining activities, reputation, ship loadouts, and other relevant data points.
 * <p>
 * This class acts as a singleton and centralizes the management of state and data related
 * to the player's progress and session-specific details.
 */
public class PlayerSession {

    public static final String PLAYER_MISSION_STATEMENT = "mission_statement";
    public static final String PLAYER_CUSTOM_TITLE = "title";
    public static final String PLAYER_ALTERNATIVE_NAME = "alternative_name";
    public static final String JOURNAL_DIR = "journal_dir";
    public static final String BINDINGS_DIR = "bindings_dir";
    private static volatile PlayerSession instance;
    /// Data managers.
    private LocationManager locationManager = LocationManager.getInstance();
    private ShipScansManager shipScans = ShipScansManager.getInstance();
    private MissionManager missions = MissionManager.getInstance();
    private BountyManager bounties = BountyManager.getInstance();
    private MiningTargetManager miningTargets = MiningTargetManager.getInstance();
    private StationMarketsManager markets = StationMarketsManager.getInstance();
    private RankAndProgressManager rankAndProgress = RankAndProgressManager.getInstance();
    private FleetCarrierManager fleetCarriers = FleetCarrierManager.getInstance();
    private BioSamplesManager bioSamples = BioSamplesManager.getInstance();
    private ShipLoadoutManager shipLoadouts = ShipLoadoutManager.getInstance();
    private GenusAnnouncementManager genusAnouncements = GenusAnnouncementManager.getInstance();
    private CargoHoldManager cargoHold = CargoHoldManager.getInstance();
    private ReputationManager reputationManager = ReputationManager.getInstance();
    private TargetLocationManager targetLocationManager = TargetLocationManager.getInstance();
    private FsdTargetManager fsdTargetManager = FsdTargetManager.getInstance();

    private PlayerSession() {
        EventBusManager.register(this);
    }

    public static PlayerSession getInstance() {
        if (instance == null) {
            synchronized (PlayerSession.class) {
                if (instance == null) {
                    instance = new PlayerSession();
                }
            }
        }
        return instance;
    }

    public void putShipScan(String key, String scan) {
        ShipScansDao.ShipScan data = new ShipScansDao.ShipScan();
        data.setScan(scan);
        data.setKey(key);
        shipScans.saveScan(data);
    }

    public String getShipScan(String key) {
        return shipScans.get(key);
    }


    public void addBountyReward(long reward) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalBountyClaimed(player.getTotalBountyClaimed() + reward);
            dao.save(player);
            return Void.class;
        });
    }

    public void addBounty(BountyDto bounty) {
        bounties.add(bounty);
    }

    public void removeBounty(BountyDto bounty) {
        bounties.remove(bounty);
    }

    public long getTotalBountyClaimed() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            return player.getTotalBountyClaimed();
        });
    }

    public void addMission(MissionDto mission) {
        missions.save(mission);
    }


    public void removeMission(Long missionId) {
        missions.remove(missionId);
    }

    public MissionDto getMission(Long missionId) {
        return missions.getMission(missionId);
    }


    public RankAndProgressDto getRankAndProgressDto() {
        return rankAndProgress.get();
    }

    public void setRankAndProgressDto(RankAndProgressDto rankAndProgressDto) {
        rankAndProgress.save(rankAndProgressDto);
    }

    @Subscribe
    public void onBounty(BountyDto data) {
        bounties.add(data);
    }

    public Set<BountyDto> getBounties() {
        return bounties.getAll();
    }


    public void saveLocation(LocationDto location) {
        if (location.getBodyId() == -1) return;
        locationManager.save(location);
    }

    public Map<Long, LocationDto> getLocations() {
        return locationManager.findByPrimaryStar(getPrimaryStarName());
    }

    public String getPrimaryStarName() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getCurrentPrimaryStar());
    }

    public void clearBounties() {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalBountyClaimed(0);
            dao.save(player);
            return Void.class;
        });
        bounties.clear();
    }

    public void setBountyCollectedLiveTime(long amount) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setBountyCollectedLifetime(amount);
            dao.save(player);
            return Void.class;
        });
    }

    public long getBountyCollectedLiveTime() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            return player.getBountyCollectedLifetime();
        });
    }

    public LocationDto getCurrentLocation() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            Long currentLocationId = player.getCurrentLocationId();
            return currentLocationId == null ? new LocationDto(-1L) : locationManager.getLocation(player.getCurrentPrimaryStar(), currentLocationId);
        });
    }

    public void setCurrentLocationId(long id) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentLocationId(id);
            dao.save(player);
            return Void.class;
        });
    }

    public CarrierDataDto getCarrierData() {
        return fleetCarriers.get();
    }

    public void setCarrierData(CarrierDataDto carrierData) {
        fleetCarriers.save(carrierData);
    }

    public Set<String> getMiningTargets() {
        return miningTargets.getAll();
    }

    public void addMiningTarget(String miningTarget) {
        miningTargets.add(miningTarget);
    }

    public void clearMiningTargets() {
        miningTargets.clear();
    }

    public GameEvents.MarketEvent getMarket() {
        return markets.findForStation(getCurrentLocation().getStationName());
    }

    public void saveMarket(GameEvents.MarketEvent data) {
        markets.save(data);
    }

    public void clearMarkets() {
        markets.clear();
    }

    public List<BioSampleDto> getBioCompletedSamples() {
        return bioSamples.listAll();
    }

    public void setBioSamples(List<BioSampleDto> data) {
        bioSamples.addInBulk(data);
    }

    public void addBioSample(BioSampleDto bioSampleDto) {
        bioSamples.add(bioSampleDto);
    }

    public void clearBioSamples() {
        bioSamples.clear();
    }

    public void setShipLoadout(LoadoutEvent event) {
        shipLoadouts.save(event);
    }

    public LoadoutEvent getShipLoadout() {
        return shipLoadouts.get();
    }


    public void clearCash() {
        bounties.clear();
        bioSamples.clear();
        markets.clear();
        shipLoadouts.clear();
        this.setShipLoadout(null);
    }

    public void setShipCargo(GameEvents.CargoEvent event) {
        cargoHold.save(event);
    }

    public GameEvents.CargoEvent getShipCargo() {
        return cargoHold.get();
    }

    public void setReputation(ReputationEvent event) {
        reputationManager.save(event);
    }

    public ReputationEvent getReputation() {
        return reputationManager.get();
    }

    public void setLastScan(LocationDto lastScan) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setLastScanId(lastScan.getBodyId());
            dao.save(player);
            return Void.class;
        });
    }

    public LocationDto getLastScan() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            return locationManager.getLocation(player.getCurrentPrimaryStar(), player.getLastScanId());
        });
    }

    public TargetLocation getTracking() {
        return targetLocationManager.get();
    }

    public void setTracking(TargetLocation tracking) {
        targetLocationManager.save(tracking);
    }


    public void setTotalHyperspaceDistance(long totalHyperspaceDistance) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalHyperspaceDistance(totalHyperspaceDistance);
            dao.save(player);
            return Void.class;
        });
    }

    public void setInsuranceClaims(int insuranceClaims) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setInsuranceClaims(insuranceClaims);
            dao.save(player);
            return Void.class;
        });
    }

    public void setTotalProfitsFromExploration(long totalProfitsFromExploration) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalProfitsFromExploration(totalProfitsFromExploration);
            dao.save(player);
            return Void.class;
        });
    }

    public void setExobiologyProfits(long exobiologyProfits) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setExobiologyProfits(exobiologyProfits);
            dao.save(player);
            return Void.class;
        });
    }

    public void setHighestSingleTransaction(long highestSingleTransaction) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setHighestSingleTransaction(highestSingleTransaction);
            dao.save(player);
            return Void.class;
        });
    }

    public String getFinalDestination() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getFinalDestination());
    }

    public void setFinalDestination(String finalDestination) {
        Database.withDao(PlayerDao.class, playerDao -> {
            PlayerDao.Player player = playerDao.get();
            player.setFinalDestination(finalDestination);
            playerDao.save(player);
            return Void.class;
        });
    }

    public void setMarketProfits(long marketProfits) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setMarketProfits(marketProfits);
            dao.save(player);
            return Void.class;
        });
    }

    public void setCurrentShip(String currentShip) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentShip(currentShip);
            dao.save(player);
            return Void.class;
        });
    }

    public String getPlayerMissionStatement() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getPlayerMissionStatement());
    }

    public void setHomeSystem() {
        LocationDto location = getCurrentLocation();
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setHomeSystemId(location.getSystemAddress());
            dao.save(player);
            return Void.class;
        });
    }

    public LocationDto getHomeSystem() {
        Long homeSystemId = Database.withDao(PlayerDao.class, dao -> dao.get().getHomeSystemId());
        return locationManager.findBySystemAddress(homeSystemId);
    }

    public void setPlayerMissionStatement(String playerMissionStatement) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setPlayerMissionStatement(playerMissionStatement);
            dao.save(player);
            return Void.class;
        });
    }

    public void setCrewWagsPayout(long crewWagsPayout) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCrewWagsPayout(crewWagsPayout);
            dao.save(player);
            return Void.class;
        });
    }

    public String getPlayerTitle() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getPlayerTitle());
    }

    public void setPlayerTitle(String playerTitle) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setPlayerTitle(playerTitle);
            dao.save(player);
            return Void.class;
        });
    }

    public void setCurrentShipName(String currentShipName) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentShipName(currentShipName);
            dao.save(player);
            return Void.class;
        });
    }


    public void setPersonalCreditsAvailable(long personalCreditsAvailable) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setPersonalCreditsAvailable(personalCreditsAvailable);
            dao.save(player);
            return Void.class;
        });
    }

    public void setShipsOwned(int shipsOwned) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setShipsOwned(shipsOwned);
            dao.save(player);
            return Void.class;
        });
    }

    public String getPlayerName() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getPlayerName());
    }

    public void setPlayerName(String playerName) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setPlayerName(playerName);
            dao.save(player);
            return Void.class;
        });
    }

    public String getLastKnownCarrierLocation() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getLastKnownCarrierLocation());
    }

    public void setLastKnownCarrierLocation(String lastKnownCarrierLocation) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setLastKnownCarrierLocation(lastKnownCarrierLocation);
            dao.save(player);
            return Void.class;
        });
    }

    public String getCarrierDepartureTime() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getCarrierDepartureTime());
    }

    public void setCarrierDepartureTime(String carrierDepartureTime) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCarrierDepartureTime(carrierDepartureTime);
            dao.save(player);
            return Void.class;
        });
    }

    public String getPlayerHighestMilitaryRank() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getPlayerHighestMilitaryRank());
    }

    public void setPlayerHighestMilitaryRank(String playerHighestMilitaryRank) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setPlayerHighestMilitaryRank(playerHighestMilitaryRank);
            dao.save(player);
            return Void.class;
        });
    }

    public void setSpeciesFirstLogged(int speciesFirstLogged) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setSpeciesFirstLogged(speciesFirstLogged);
            dao.save(player);
            return Void.class;
        });
    }

    public void setTotalSystemsVisited(int totalSystemsVisited) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalSystemsVisited(totalSystemsVisited);
            dao.save(player);
            return Void.class;
        });
    }

    public void setTotalBountyClaimed(long totalBountyClaimed) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalBountyClaimed(totalBountyClaimed);
            dao.save(player);
            return Void.class;
        });
    }

    public void setGoodsSoldThisSession(int goodsSoldThisSession) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setGoodsSoldThisSession(goodsSoldThisSession);
            dao.save(player);
            return Void.class;
        });
    }

    public void setTotalDistanceTraveled(double totalDistanceTraveled) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalDistanceTraveled(totalDistanceTraveled);
            dao.save(player);
            return Void.class;
        });
    }

    public void setFsdTarget(FsdTarget fsdTarget) {
        fsdTargetManager.save(fsdTarget);
    }

    public FsdTarget getFsdTarget() {
        return fsdTargetManager.get();
    }

    public Boolean isRadioTransmissionOn() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getRadioTransmissionOn());
    }

    public void setRadioTransmissionOn(Boolean radioTransmissionOn) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setRadioTransmissionOn(radioTransmissionOn);
            dao.save(player);
            return Void.class;
        });
    }

    public Boolean isMiningAnnouncementOn() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().isMiningAnnouncementOn());
    }

    public void setMiningAnnouncementOn(Boolean miningAnnouncementOn) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setMiningAnnouncementOn(miningAnnouncementOn);
            dao.save(player);
            return Void.class;
        });
    }

    public Boolean isNavigationAnnouncementOn() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().isNavigationAnnouncementOn());
    }

    public void setNavigationAnnouncementOn(Boolean navigationAnnouncementOn) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setNavigationAnnouncementOn(navigationAnnouncementOn);
            dao.save(player);
            return Void.class;
        });
    }

    public Boolean isDiscoveryAnnouncementOn() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().isDiscoveryAnnouncementOn());
    }

    public void setDiscoveryAnnouncementOn(Boolean discoveryAnnouncementOn) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setDiscoveryAnnouncementOn(discoveryAnnouncementOn);
            dao.save(player);
            return Void.class;
        });
    }


    public Boolean isRouteAnnouncementOn() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().isRouteAnnouncementOn());
    }

    public void setRouteAnnouncementOn(Boolean routeAnnouncementOn) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setRouteAnnouncementOn(routeAnnouncementOn);
            dao.save(player);
            return Void.class;
        });
    }

    public void setGenusPaymentAnnounced(String genus) {
        genusAnouncements.put(genus, true);
    }

    public void clearGenusPaymentAnnounced() {
        genusAnouncements.clear();
    }


    public void setCurrentWealth(long currentWealth) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentWealth(currentWealth);
            dao.save(player);
            return Void.class;
        });
    }

    public Boolean paymentHasBeenAnnounced(String genus) {

        Boolean b = genusAnouncements.get(genus);
        return b != null && b;
    }

    public void setGameVersion(String gameversion) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setGameVersion(gameversion);
            dao.save(player);
            return Void.class;
        });
    }

    public void setGameBuild(String gameBuild) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setGameBuild(gameBuild);
            dao.save(player);
            return Void.class;
        });
    }

    public String getGameVersion() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getGameVersion());
    }

    public String getGameBuild() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getGameBuild());
    }

    public void setInGameName(String inGameName) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setInGameName(inGameName);
            dao.save(player);
            return Void.class;
        });
    }

    public String getInGameName() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getInGameName());
    }

    public void setCurrentPrimaryStarName(String starName) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentPrimaryStar(starName);
            dao.save(player);
            return Void.class;
        });
    }

    public void setJournalPath(String path) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setJournalDirectory(path);
            dao.save(player);
            return Void.class;
        });
    }

    public Path getJournalPath() {
        return Database.withDao(PlayerDao.class, dao -> {
            String directory = trimToNull(dao.get().getJournalDirectory());
            if(OsDetector.getOs() == OsDetector.OS.WINDOWS) {
                return directory == null ? Paths.get(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous") : Paths.get(directory);
            } else if(OsDetector.getOs() == OsDetector.OS.LINUX) {
                return directory == null ? Paths.get(System.getProperty("user.home"), ".var", "app", "elite.intel.app", "ed-journal") : Paths.get(directory);
            } else {
                return directory == null ? Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Frontier Developments", "Elite Dangerous") : Paths.get(directory);
            }
        });
    }

    public void setBindingsDir(String path) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setBindingsDirectory(path);
            dao.save(player);
            return Void.class;
        });
    }

    public Path getBindingsDir() {
        return Database.withDao(PlayerDao.class, dao -> {
            String directory = trimToNull(dao.get().getBindingsDirectory());
            if(OsDetector.getOs() == OsDetector.OS.WINDOWS) {
                return directory == null ? Paths.get(System.getProperty("user.home"), "AppData", "Local", "Frontier Developments", "Elite Dangerous", "Options", "Bindings") : Paths.get(directory);
            } else if(OsDetector.getOs() == OsDetector.OS.LINUX){
                return directory == null ? Paths.get(System.getProperty("user.home"), ".var", "app", "elite.intel.app", "ed-bindings") : Paths.get(directory);
            } else {
                return directory == null ? Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Frontier Developments", "Elite Dangerous", "Options", "Bindings") : Paths.get(directory);
            }
        });
    }

    public void setAlternativeName(String alternativeName) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setAlternativeName(alternativeName);
            dao.save(player);
            return Void.class;
        });
    }

    public String getAlternativeName() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getAlternativeName());
    }

    public LocationDto getPrimaryStarLocation() {
        return locationManager.findPrimaryStar(getPrimaryStarName());
    }

    public void clearShipScans() {
        shipScans.clear();
    }

    public void setCarrierStats(CarrierStatsEvent event) {
        fleetCarriers.setCarrierStats(event);
    }

    public Map<String, String> asMap() {
        Map<String, String> result = new HashMap<>();
        result.put(PLAYER_ALTERNATIVE_NAME, getAlternativeName());
        result.put(PLAYER_MISSION_STATEMENT, getPlayerMissionStatement());
        result.put(PLAYER_CUSTOM_TITLE, getPlayerTitle());
        result.put(JOURNAL_DIR, getJournalPath().toString());
        result.put(BINDINGS_DIR, getBindingsDir().toString());
        return result;
    }

    public long getHighestTransaction() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getHighestSingleTransaction());
    }

    public long getTradeProfits() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getMarketProfits());
    }

    public long getPersonalCredits() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getPersonalCreditsAvailable());
    }

    public double getTotalDistanceTraveled() {
        return Database.withDao(PlayerDao.class, playerDao -> playerDao.get().getTotalDistanceTraveled());
    }

    public long getTotalHyperspaceDistance() {
        return Database.withDao(PlayerDao.class, playerDao -> playerDao.get().getTotalHyperspaceDistance());
    }

    public long getTotalProfitsFromExploration() {
        return Database.withDao(PlayerDao.class, playerDao -> playerDao.get().getTotalProfitsFromExploration());
    }

    public long getTotalSystemsVisited() {
        return Database.withDao(PlayerDao.class, playerDao -> playerDao.get().getTotalSystemsVisited());
    }

    public long getTotalExobiologyProfits() {
        return Database.withDao(PlayerDao.class, playerDao -> playerDao.get().getExobiologyProfits());
    }

    public String getLocalTtsAddress() {
        return Database.withDao(PlayerDao.class, playerDao -> playerDao.get().getLocalTtsServer());
    }

    public void setLocalTtsAddress(String address) {
        Database.withDao(PlayerDao.class, playerDao -> {
            PlayerDao.Player player = playerDao.get();
            player.setLocalTtsServer(address);
            playerDao.save(player);
            return Void.class;
        });
    }

    public record GalacticCoordinates(double x, double y, double z) {

    }
}
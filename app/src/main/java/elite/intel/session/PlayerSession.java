package elite.intel.session;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.spansh.market.StationMarketDto;
import elite.intel.db.dao.PlayerDao;
import elite.intel.db.dao.ShipScansDao;
import elite.intel.db.managers.*;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.FsdTarget;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.journal.events.*;
import elite.intel.gameapi.journal.events.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerSession  {

    private static volatile PlayerSession instance;

    /// Data managers.
    private LocationManager locationData = LocationManager.getInstance();
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


    public void addBountyReward(long totalReward) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalBountyClaimed(totalReward);
            dao.save(player);
            return null;
        });
    }

    public void addBounty(BountyDto bounty) {
        bounties.add(bounty);
    }

    public void removeBounty(BountyDto bounty) {
        bounties.remove(bounty);
    }

    public long getBountyCollectedThisSession() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            return player.getBountyCollectedThisSession();
        });
    }

    public void addMission(MissionDto mission) {
        missions.save(mission);
    }

    public Map<Long, MissionDto> getMissions() {
        return missions.getMissions();
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

    public Set<String> getTargetFactions() {
        return missions.getTargetFactions();
    }

    public void saveLocation(LocationDto location) {
        if (location.getBodyId() == -1) return;
        locationData.save(location);
    }

    public Map<Long, LocationDto> getLocations() {
        return locationData.findByPrimaryStar(getPrimaryStarName());
    }

    public String getPrimaryStarName() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getCurrentPrimaryStar());
    }

    public LocationDto getLocation(long id, String primaryStarName) {
        return locationData.getLocation(primaryStarName, id);
    }

    public void clearBounties() {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setBountyCollectedThisSession(0);
            dao.save(player);
            return null;
        });
        setTotalBountyProfit(0);
        setTotalBountyClaimed(0);
        bounties.clear();
    }

    public LocationDto getCurrentLocation() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            Long currentLocationId = player.getCurrentLocationId();
            return currentLocationId == null ? new LocationDto(-1) : getLocation(currentLocationId, player.getCurrentPrimaryStar());
        });
    }

    public void setCurrentLocationId(long id) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentLocationId(id);
            dao.save(player);
            return null;
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

    public List<StationMarketDto> getMarkets() {
        return markets.findForStation(getCurrentLocation().getStationName());
    }

    public void setMarkets(List<StationMarketDto> data) {
        markets.addList(data);
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
            return null;
        });
    }

    public LocationDto getLastScan() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            return getLocation(player.getLastScanId(), player.getCurrentPrimaryStar());
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
            return null;
        });
    }

    public void setInsuranceClaims(int insuranceClaims) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setInsuranceClaims(insuranceClaims);
            dao.save(player);
            return null;
        });
    }

    public void setTotalProfitsFromExploration(long totalProfitsFromExploration) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalProfitsFromExploration(totalProfitsFromExploration);
            dao.save(player);
            return null;
        });
    }

    public void setExobiologyProfits(long exobiologyProfits) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setExobiologyProfits(exobiologyProfits);
            dao.save(player);
            return null;
        });
    }

    public void setHighestSingleTransaction(long highestSingleTransaction) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setHighestSingleTransaction(highestSingleTransaction);
            dao.save(player);
            return null;
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
            return null;
        });
    }

    public void setMarketProfits(long marketProfits) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setMarketProfits(marketProfits);
            dao.save(player);
            return null;
        });
    }

    public void setCurrentShip(String currentShip) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentShip(currentShip);
            dao.save(player);
            return null;
        });
    }

    public void setTotalBountyProfit(long totalBountyProfit) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalBountyProfit(totalBountyProfit);
            dao.save(player);
            return null;
        });
    }

    public String getPlayerMissionStatement() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getPlayerMissionStatement());
    }

    public void setPlayerMissionStatement(String playerMissionStatement) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setPlayerMissionStatement(playerMissionStatement);
            dao.save(player);
            return null;
        });
    }

    public void setCrewWagsPayout(long crewWagsPayout) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCrewWagsPayout(crewWagsPayout);
            dao.save(player);
            return null;
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
            return null;
        });
    }

    public void setCurrentShipName(String currentShipName) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentShipName(currentShipName);
            dao.save(player);
            return null;
        });
    }


    public void setPersonalCreditsAvailable(long personalCreditsAvailable) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setPersonalCreditsAvailable(personalCreditsAvailable);
            dao.save(player);
            return null;
        });
    }

    public void setShipsOwned(int shipsOwned) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setShipsOwned(shipsOwned);
            dao.save(player);
            return null;
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
            return null;
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
            return null;
        });
    }


    public void setShipFuelLevel(double shipFuelLevel) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setShipFuelLevel(shipFuelLevel);
            dao.save(player);
            return null;
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
            return null;
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
            return null;
        });
    }

    public void setSpeciesFirstLogged(int speciesFirstLogged) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setSpeciesFirstLogged(speciesFirstLogged);
            dao.save(player);
            return null;
        });
    }

    public void setShipCargoCapacity(int shipCargoCapacity) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setShipCargoCapacity(shipCargoCapacity);
            dao.save(player);
            return null;
        });
    }

    public void setTotalSystemsVisited(int totalSystemsVisited) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalSystemsVisited(totalSystemsVisited);
            dao.save(player);
            return null;
        });
    }

    public void setTotalBountyClaimed(long totalBountyClaimed) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalBountyClaimed(totalBountyClaimed);
            dao.save(player);
            return null;
        });
    }

    public void setGoodsSoldThisSession(int goodsSoldThisSession) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setGoodsSoldThisSession(goodsSoldThisSession);
            dao.save(player);
            return null;
        });
    }

    public void setTotalDistanceTraveled(double totalDistanceTraveled) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setTotalDistanceTraveled(totalDistanceTraveled);
            dao.save(player);
            return null;
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
            return null;
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
            return null;
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
            return null;
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
            return null;
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
            return null;
        });
    }

    public void setGenusPaymentAnnounced(String genus) {
        genusAnouncements.put(genus, true);
    }

    public void clearGenusPaymentAnnounced() {
        genusAnouncements.clear();
    }

    public GalacticCoordinates getGalacticCoordinates() {

        Status status = Status.getInstance();
        CarrierDataDto carrierInfo = getCarrierData();

        if (status.isDocked()) {
            // we are on the fleet carrier
            return new GalacticCoordinates(carrierInfo.getX(), carrierInfo.getY(), carrierInfo.getZ());
        }


        Map<Long, LocationDto> locations = getLocations();
        for (LocationDto location : locations.values()) {
            if (location.getLocationType().equals(LocationDto.LocationType.PRIMARY_STAR)) {
                return new GalacticCoordinates(location.getX(), location.getY(), location.getZ());
            }
        }
        return null;
    }


    public void setCurrentWealth(long currentWealth) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setCurrentWealth(currentWealth);
            dao.save(player);
            return null;
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
            return null;
        });
    }

    public String getGameVersion() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getGameVersion());
    }

    public void setInGameName(String inGameName) {
        Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            player.setInGameName(inGameName);
            dao.save(player);
            return null;
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
            return null;
        });
    }

    public LocationDto getPrimaryStarLocation() {
        return locationData.findPrimaryStar(getPrimaryStarName());
    }

    public void clearShipScans() {
        shipScans.clear();
    }

    public void setCarrierStats(CarrierStatsEvent event) {
        fleetCarriers.setCarrierStats(event);
    }

    public record GalacticCoordinates(double x, double y, double z) {

    }
}
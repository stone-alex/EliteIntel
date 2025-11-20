package elite.intel.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.reflect.TypeToken;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.db.Locations;
import elite.intel.db.util.Database;
import elite.intel.db.dao.PlayerDao;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.FsdTarget;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.journal.events.*;
import elite.intel.gameapi.journal.events.dto.*;

import java.util.*;

/**
 * The PlayerSession class manages the game session data for a player.
 * It is implemented as a singleton to ensure consistent access across the application.
 * The class handles persistence of session data, including player's progress,
 * gameplay metrics, and interactions within the game world.
 */
public class PlayerSession extends SessionPersistence implements java.io.Serializable {
    // Existing constants
    public static final String CARRIER_DEPARTURE_TIME = "carrier_departure_time";
    public static final String MISSIONS = "player_missions";
    public static final String PROFILE = "profile";
    public static final String PERSONALITY = "personality";
    public static final String SHIP_SCANS = "shipScans";
    public static final String TARGET_FACTIONS = "targetFactions";
    public static final String SESSION_DIR = "session/";
    public static final String TRACKING = "tracking";
    public static final String RANK_AND_PROGRESS_DTO = "rankAndProgressDto";
    public static final String MARKETS = "markets";
    public static final String FSD_TARGET = "fsd_target";
    public static final String TARGET_MARKET_STATION = "target_market_station";
    private static final String SESSION_FILE = "player_session.json";
    private static final String REPUTATION = "reputation";
    private static final String BIO_SAMPLES = "bio_samples";
    private static final String SHIP_LOADOUT = "ship_loadout";
    private static final String SHIP_CARGO = "ship_cargo";
    private static final String FRIENDS_STATUS = "friends_status";
    private static final String CARRIER_STATS = "carrier_stats";
    private static final String BOUNTIES = "bounties";
    private static final String MINING_TARGETS = "miningTargets";
    private static volatile PlayerSession instance;


    // Existing fields
    private final Map<String, String> shipScans = new HashMap<>();
    private final Map<Long, MissionDto> missions = new LinkedHashMap<>();
    private final Set<String> targetFactions = new LinkedHashSet<>();
    private final Set<BountyDto> bounties = new LinkedHashSet<>();
    private final Set<String> miningTargets = new HashSet<>();
    private List<StationMarket> markets = new ArrayList<>();
    private RankAndProgressDto rankAndProgressDto = new RankAndProgressDto();
    private CarrierDataDto carrierData = new CarrierDataDto();
    private List<BioSampleDto> bioSamples = new ArrayList<>();
    private LoadoutEvent loadout;
    private GameEvents.CargoEvent shipCargo;
    private ReputationEvent reputation;
    private TargetLocation tracking = new TargetLocation();
    private Map<String, String> friendsStatus = new HashMap<>();
    private FsdTarget fsdTarget;
    private Map<String, Boolean> genusPaymentAnnounced = new HashMap<>();
    private StationMarket targetMarketStation= new StationMarket();

    private Locations locationData = Locations.getInstance();

    private PlayerSession() {
        super(SESSION_DIR);
        ensureFileAndDirectoryExist(SESSION_FILE);
        registerField(SHIP_SCANS, this::getShipScans, v -> {
            shipScans.clear();
            shipScans.putAll((Map<String, String>) v);
        }, new TypeToken<Map<String, String>>() {
        }.getType());
        registerField(MISSIONS, this::getMissions, v -> {
            missions.clear();
            missions.putAll((Map<Long, MissionDto>) v);
        }, new TypeToken<Map<Long, MissionDto>>() {
        }.getType());


        registerField(TARGET_FACTIONS, this::getTargetFactions, v -> {
            targetFactions.clear();
            targetFactions.addAll((Set<String>) v);
        }, new TypeToken<Set<String>>() {
        }.getType());
        registerField(BOUNTIES, this::getBounties, v -> {
            bounties.clear();
            bounties.addAll((Set<BountyDto>) v);
        }, new TypeToken<Set<BountyDto>>() {
        }.getType());
        registerField(MINING_TARGETS, this::getMiningTargets, v -> {
            miningTargets.clear();
            miningTargets.addAll((Set<String>) v);
        }, new TypeToken<Set<String>>() {
        }.getType());
        registerField(MARKETS, this::getMarkets, this::setMarkets, new TypeToken<List<StationMarket>>() {
        }.getType());
        registerField(BIO_SAMPLES, this::getBioCompletedSamples, this::setBioSamples, new TypeToken<List<BioSampleDto>>() {
        }.getType());
        registerField(SHIP_LOADOUT, this::getShipLoadout, this::setShipLoadout, new TypeToken<LoadoutEvent>() {
        }.getType());
        registerField(SHIP_CARGO, this::getShipCargo, this::setShipCargo, GameEvents.CargoEvent.class);
        registerField(REPUTATION, this::getReputation, this::setReputation, ReputationEvent.class);
        registerField(TRACKING, this::getTracking, this::setTracking, TargetLocation.class);
        registerField(RANK_AND_PROGRESS_DTO, this::getRankAndProgressDto, this::setRankAndProgressDto, RankAndProgressDto.class);
        registerField(CARRIER_STATS, this::getCarrierData, this::setCarrierData, CarrierDataDto.class);
        registerField(TARGET_MARKET_STATION, this::getTargetMarketStation, this::setTargetMarketStation, StationMarket.class);
        registerField(TARGET_MARKET_STATION, this::getTargetMarketStation, this::setTargetMarketStation, StationMarket.class);
        registerField(FRIENDS_STATUS, this::getFriendsStatus, v -> {
            friendsStatus.clear();
            friendsStatus.putAll(v);
        }, new TypeToken<Map<String, String>>() {
        }.getType());
        registerField(FSD_TARGET, this::getFsdTarget, this::setFsdTarget, FsdTarget.class);

        loadSavedStateFromDisk();
        EventBusManager.register(this);
        addShutdownHook();
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

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    private void loadSavedStateFromDisk() {
        loadSession(PlayerSession.this::loadFields);
    }

    public void putShipScan(String shipName, String scan) {
        shipScans.put(shipName, scan);
        save();
    }

    public String getShipScan(String shipName) {
        return shipScans.get(shipName);
    }

    private Map<String, String> getShipScans() {
        return shipScans;
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
        save();
    }

    public void removeBounty(BountyDto bounty) {
        bounties.remove(bounty);
        save();
    }

    public long getBountyCollectedThisSession() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            return player.getBountyCollectedThisSession();
        });
    }

    public void addMission(MissionDto mission) {
        missions.put(mission.getMissionId(), mission);
        save();
    }

    public Map<Long, MissionDto> getMissions() {
        return missions;
    }

    public void removeMission(Long missionId) {
        missions.remove(missionId);
        save();
    }

    public MissionDto getMission(Long missionId) {
        return missions.get(missionId);
    }


    public RankAndProgressDto getRankAndProgressDto() {
        return rankAndProgressDto;
    }

    public void setRankAndProgressDto(RankAndProgressDto rankAndProgressDto) {
        this.rankAndProgressDto = rankAndProgressDto;
        save();
    }

    public void clearShipScans() {
        shipScans.clear();
        save();
    }

    public void clearOnShutDown() {
        //
    }

    public void setCarrierStats(CarrierStatsEvent event) {
        CarrierStatsEvent.Finance finance = event.getFinance();
        CarrierDataDto carrierData = getCarrierData();
        carrierData.setCallSign(event.getCallsign());
        carrierData.setCarrierName(event.getName());
        carrierData.setCarrierType(event.getCarrierType());
        carrierData.setDockingAccess(event.getDockingAccess());
        carrierData.setCurrentJumpRange(event.getJumpRangeCurr());
        carrierData.setMaxJumpRange(event.getJumpRangeMax());
        carrierData.setAllowNotorious(event.isAllowNotorious());
        carrierData.setPendingDecommission(event.isPendingDecommission());
        carrierData.setFuelLevel(event.getFuelLevel());

        if (event.getSpaceUsage() != null) {
            CarrierStatsEvent.SpaceUsage spaceUsage = event.getSpaceUsage();
            carrierData.setCargoSpaceUsed(spaceUsage.getCargo());
            carrierData.setCargoSpaceReserved(spaceUsage.getCargoSpaceReserved());
            carrierData.setShipRacks(spaceUsage.getShipPacks());
            carrierData.setModulePacks(spaceUsage.getModulePacks());
            carrierData.setFreeSpaceInCargo(spaceUsage.getFreeSpace());
            carrierData.setCargoCapacity(spaceUsage.getTotalCapacity());
        }

        if (finance != null) {
            carrierData.setTotalBalance(finance.getCarrierBalance());
            carrierData.setReserveBalance(finance.getReserveBalance());
            carrierData.setMarketBalance(finance.getAvailableBalance());
            carrierData.setPioneerSupplyTax(finance.getTaxRate_pioneersupplies());
            carrierData.setShipYardSupplyTax(finance.getTaxRate_shipyard());
            carrierData.setRearmSupplyTax(finance.getTaxRate_rearm());
            carrierData.setRepairSupplyTax(finance.getTaxRate_repair());
            carrierData.setRefuelSupplyTax(finance.getTaxRate_refuel());
            setCarrierData(carrierData);
        }
        save();
    }

    @Subscribe public void onLoadGame(LoadGameEvent event) {
        loadSavedStateFromDisk();
    }

    @Subscribe public void onLoadSession(LoadSessionEvent event) {
        loadSavedStateFromDisk();
    }

    @Subscribe
    public void onBounty(BountyDto data) {
        bounties.add(data);
        save();
    }

    public Set<BountyDto> getBounties() {
        return bounties;
    }

    public void addTargetFaction(String faction) {
        targetFactions.add(faction);
        save();
    }

    public void setTargetFactions(Set<String> factions) {
        targetFactions.addAll(factions);
        save();
    }

    public Set<String> getTargetFactions() {
        if (getMissions() == null || getMissions().isEmpty()) {
            targetFactions.clear();
        }
        return targetFactions;
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
        save();
    }

    public LocationDto getCurrentLocation() {
        return Database.withDao(PlayerDao.class, dao -> {
            PlayerDao.Player player = dao.get();
            Long currentLocationId = player.getCurrentLocationId();
            return currentLocationId == null ? new LocationDto(-1): getLocation(currentLocationId, player.getCurrentPrimaryStar());
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
        return carrierData == null ? new CarrierDataDto() : carrierData;
    }

    public void setCarrierData(CarrierDataDto carrierData) {
        this.carrierData = carrierData;
        save();
    }

    public Set<String> getMiningTargets() {
        return miningTargets;
    }

    public void addMiningTarget(String miningTarget) {
        if (miningTarget == null || miningTarget.isEmpty()) return;
        miningTargets.add(miningTarget.toLowerCase());
        save();
    }

    public void clearMiningTargets() {
        miningTargets.clear();
        save();
    }

    public List<StationMarket> getMarkets() {
        return markets;
    }

    public void setMarkets(List<StationMarket> markets) {
        this.markets = markets;
    }

    public void clearMarkets() {
        markets.clear();
    }

    public List<BioSampleDto> getBioCompletedSamples() {
        return bioSamples;
    }

    public void setBioSamples(List<BioSampleDto> bioSamples) {
        this.bioSamples = bioSamples;
        save();
    }

    public void addBioSample(BioSampleDto bioSampleDto) {
        this.bioSamples.add(bioSampleDto);
        save();
    }

    public void clearBioSamples() {
        this.bioSamples.clear();
        save();
    }

    public void setShipLoadout(LoadoutEvent event) {
        this.loadout = event;
        save();
    }

    public LoadoutEvent getShipLoadout() {
        return loadout;
    }


    public void clearCash() {
        this.bioSamples.clear();
        this.bounties.clear();
        this.carrierData = new CarrierDataDto();
        this.loadout = null;
        this.markets.clear();
        this.missions.clear();
        this.rankAndProgressDto = new RankAndProgressDto();
        this.setShipLoadout(null);
        this.shipScans.clear();
        this.targetFactions.clear();
        this.save();
    }

    public void setShipCargo(GameEvents.CargoEvent event) {
        this.shipCargo = event;
        save();
    }

    public GameEvents.CargoEvent getShipCargo() {
        return this.shipCargo;
    }

    public void setReputation(ReputationEvent event) {
        this.reputation = event;
        save();
    }

    public ReputationEvent getReputation() {
        return this.reputation;
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
        return tracking == null ? new TargetLocation() : tracking;
    }

    public void setTracking(TargetLocation tracking) {
        this.tracking = tracking;
        save();
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

    public Map<String, String> getFriendsStatus() {
        return friendsStatus;
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
        Database.withDao(PlayerDao.class, dao ->{
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

    public void setFsdTarget(FsdTarget json) {
        this.fsdTarget = json;
        save();
    }

    public FsdTarget getFsdTarget() {
        return fsdTarget;
    }

    public Boolean isRadioTransmissionOn() {
        return Database.withDao(PlayerDao.class, dao -> dao.get().getRadioTransmissionOn());
    }

    public void setRadioTransmissionOn(Boolean radioTransmissionOn) {
        Database.withDao(PlayerDao.class, dao ->{
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
        Database.withDao(PlayerDao.class, dao ->{
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
        Database.withDao(PlayerDao.class, dao ->{
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
        Database.withDao(PlayerDao.class, dao ->{
            PlayerDao.Player player = dao.get();
            player.setRouteAnnouncementOn(routeAnnouncementOn);
            dao.save(player);
            return null;
        });
    }

    public StationMarket getTargetMarketStation() {
        return targetMarketStation;
    }

    public void setTargetMarketStation(StationMarket targetMarketStation) {
        this.targetMarketStation = targetMarketStation;
        save();
    }


    public void setGenusPaymentAnnounced(String genus) {
        genusPaymentAnnounced.put(genus, true);
        save();
    }

    public void clearGenusPaymentAnnounced() {
        this.genusPaymentAnnounced.clear();
        save();
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
        Database.withDao(PlayerDao.class, dao ->{
            PlayerDao.Player player = dao.get();
            player.setCurrentWealth(currentWealth);
            dao.save(player);
            return null;
        });
    }

    public Boolean paymentHasBeenAnnounced(String genus) {
        Boolean b = genusPaymentAnnounced.get(genus);
        return b != null && b;
    }

    @Subscribe public void onShutDownEvent(ShutdownEvent event) {
        save();
    }

    public void setGameVersion(String gameversion) {
        Database.withDao(PlayerDao.class, dao ->{
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
        Database.withDao(PlayerDao.class, dao ->{
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

    public record GalacticCoordinates(double x, double y, double z) {

    }
}
package elite.intel.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.reflect.TypeToken;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.gameapi.gamestate.events.NavRouteDto;
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
    private static volatile PlayerSession instance;
    private static final String SESSION_FILE = "player_session.json";

    // Existing constants
    public static final String CARRIER_DEPARTURE_TIME = "carrier_departure_time";
    public static final String FINAL_DESTINATION = "final_destination";
    public static final String MISSIONS = "player_missions";
    public static final String PROFILE = "profile";
    public static final String PERSONALITY = "personality";
    public static final String SHIP_FUEL_LEVEL = "ship_fuel_level";
    public static final String INSURANCE_CLAIMS = "insurance_claims";
    public static final String SHIPS_OWNED = "ships_owned";
    public static final String TOTAL_BOUNTY_CLAIMED = "total_bounty_claimed";
    public static final String TOTAL_BOUNTY_PROFIT = "total_bounty_profit";
    public static final String TOTAL_DISTANCE_TRAVELED = "total_distance_traveled_in_light_years";
    public static final String TOTAL_SYSTEMS_VISITED = "total_systems_visited";
    public static final String TOTAL_HYPERSPACE_DISTANCE = "total_hyperspace_distance_in_light_years";
    public static final String TOTAL_PROFITS_FROM_EXPLORATION = "total_profits_from_exploration";
    public static final String SPECIES_FIRST_LOGGED = "species_first_logged";
    public static final String EXOBIOLOGY_PROFITS = "exobiology_profits";
    public static final String GOODS_SOLD_THIS_SESSION = "goods_sold_this_session";
    public static final String HIGHEST_SINGLE_TRANSACTION = "highest_single_transaction";
    public static final String MARKET_PROFITS = "market_profits";
    public static final String CREW_WAGS_PAYOUT = "crew_wags_payout";
    public static final String SHIP_CARGO_CAPACITY = "ship_cargo_capacity";
    public static final String PLAYER_CUSTOM_TITLE = "player_title";
    public static final String PLAYER_HIGHEST_MILITARY_RANK = "player_highest_military_rank";
    public static final String PLAYER_NAME = "player_name";
    public static final String PLAYER_MISSION_STATEMENT = "player_mission_statement";
    public static final String CURRENT_SHIP = "current_ship";
    public static final String CURRENT_SHIP_NAME = "current_ship_name";
    public static final String PERSONAL_CREDITS_AVAILABLE = "personal_credits_available";
    public static final String SHIP_SCANS = "shipScans";
    public static final String TARGET_FACTIONS = "targetFactions";
    public static final String CARRIER_LOCATION = "last_known_carrier_location";
    private static final String CURRENT_LOCATION = "current_location";
    private static final String REPUTATION = "reputation";
    private static final String ROUTE_MAP = "routeMap";
    private static final String STELLAR_OBJECTS = "locations";
    private static final String BIO_SAMPLES = "bio_samples";
    private static final String SHIP_LOADOUT = "ship_loadout";
    private static final String SHIP_CARGO = "ship_cargo";
    private static final String FRIENDS_STATUS = "friends_status";
    private static final String CARRIER_STATS = "carrier_stats";
    private static final String BOUNTIES = "bounties";
    private static final String MINING_TARGETS = "miningTargets";
    private static final String HOME_SYSTEM = "home_system";
    public static final String SESSION_DIR = "session/";
    public static final String TRACKING = "tracking";
    public static final String BOUNTY_COLLECTED_THIS_SESSION = "bountyCollectedThisSession";
    public static final String RANK_AND_PROGRESS_DTO = "rankAndProgressDto";
    public static final String MARKETS = "markets";
    public static final String FSD_TARGET = "fsd_target";
    public static final String JUMPING_TO_STARSYSTEM = "jumping_to_starsystem";

    // Existing fields
    private final Map<String, String> shipScans = new HashMap<>();
    private final Map<Long, MissionDto> missions = new LinkedHashMap<>();
    private final Map<Long, LocationDto> locations = new HashMap<>();
    private final Map<Integer, NavRouteDto> routeMap = new LinkedHashMap<>();
    private final Set<String> targetFactions = new LinkedHashSet<>();
    private final Set<BountyDto> bounties = new LinkedHashSet<>();
    private final Set<String> miningTargets = new HashSet<>();
    private List<StationMarket> markets = new ArrayList<>();
    private long bountyCollectedThisSession = 0;
    private RankAndProgressDto rankAndProgressDto = new RankAndProgressDto();
    private LocationDto homeSystem = new LocationDto();
    private long lastScanId = -1;
    private CarrierDataDto carrierData = new CarrierDataDto();
    private List<BioSampleDto> bioSamples = new ArrayList<>();
    private LoadoutEvent loadout;

    private GameEvents.CargoEvent shipCargo;
    private ReputationEvent reputation;
    private TargetLocation tracking = new TargetLocation();
    private Long currentLocationId = -1L;

    // New fields for state replacement
    private long totalHyperspaceDistance = 0;
    private int insuranceClaims = 0;
    private long totalProfitsFromExploration = 0;
    private long exobiologyProfits = 0;
    private long highestSingleTransaction = 0;
    private String finalDestination = "";
    private long marketProfits = 0;
    private String currentShip = "";
    private long totalBountyProfit = 0;
    private String playerMissionStatement = "";
    private long crewWagsPayout = 0;
    private String playerTitle = "";
    private String currentShipName = "";
    private long personalCreditsAvailable = 0;
    private int shipsOwned = 0;
    private String playerName = "";
    private String lastKnownCarrierLocation = "";
    private double shipFuelLevel = 0;
    private Map<String, String> friendsStatus = new HashMap<>();
    private String carrierDepartureTime = "";
    private String jumpingToStarSystem = "";
    private String playerHighestMilitaryRank = "";
    private int speciesFirstLogged = 0;
    private int shipCargoCapacity = 0;
    private int totalSystemsVisited = 0;
    private int totalBountyClaimed = 0;
    private int goodsSoldThisSession = 0;
    private double totalDistanceTraveled = 0.0;
    private String fsdTarget;
    private Boolean isRadioTransmissionOn;
    private Boolean isMiningAnnouncementOn = true;
    private Boolean isNavigationAnnouncementOn = true;
    private Boolean isDiscoveryAnnouncementOn = true;
    private Boolean isRouteAnnouncementOn = true;



    private PlayerSession() {
        super(SESSION_DIR);
        ensureFileAndDirectoryExist(SESSION_FILE);
        loadSavedStateFromDisk();
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
        registerField(ROUTE_MAP, this::getRoute, v -> {
            routeMap.clear();
            routeMap.putAll((Map<Integer, NavRouteDto>) v);
        }, new TypeToken<Map<Integer, NavRouteDto>>() {
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
        registerField(STELLAR_OBJECTS, this::getLocations, v -> {
            locations.clear();
            locations.putAll((Map<Long, LocationDto>) v);
        }, new TypeToken<Map<Long, LocationDto>>() {
        }.getType());
        registerField(MINING_TARGETS, this::getMiningTargets, v -> {
            miningTargets.clear();
            miningTargets.addAll((Set<String>) v);
        }, new TypeToken<Set<String>>() {}.getType());
        registerField(MARKETS, this::getMarkets, this::setMarkets, new TypeToken<List<StationMarket>>() {}.getType());
        registerField(BIO_SAMPLES, this::getBioCompletedSamples, this::setBioSamples, new TypeToken<List<BioSampleDto>>() {}.getType());
        registerField(HOME_SYSTEM, this::getHomeSystem, this::setHomeSystem, LocationDto.class);
        registerField(SHIP_LOADOUT, this::getShipLoadout, this::setShipLoadout, new TypeToken<LoadoutEvent>() {}.getType());
        registerField(SHIP_CARGO, this::getShipCargo, this::setShipCargo, GameEvents.CargoEvent.class);
        registerField(REPUTATION, this::getReputation, this::setReputation, ReputationEvent.class);
        registerField(TRACKING, this::getTracking, this::setTracking, TargetLocation.class);
        registerField(CURRENT_LOCATION, this::getCurrentLocationId, this::setCurrentLocationId, Long.class);
        registerField(BOUNTY_COLLECTED_THIS_SESSION, this::getBountyCollectedThisSession, this::setBountyCollectedThisSession, Long.class);
        registerField(RANK_AND_PROGRESS_DTO, this::getRankAndProgressDto, this::setRankAndProgressDto, RankAndProgressDto.class);
        registerField(CARRIER_STATS, this::getCarrierData, this::setCarrierData, CarrierDataDto.class);

        // New field registrations
        registerField(TOTAL_HYPERSPACE_DISTANCE, this::getTotalHyperspaceDistance, this::setTotalHyperspaceDistance, Long.class);
        registerField(INSURANCE_CLAIMS, this::getInsuranceClaims, this::setInsuranceClaims, Integer.class);
        registerField(TOTAL_PROFITS_FROM_EXPLORATION, this::getTotalProfitsFromExploration, this::setTotalProfitsFromExploration, Long.class);
        registerField(EXOBIOLOGY_PROFITS, this::getExobiologyProfits, this::setExobiologyProfits, Long.class);
        registerField(HIGHEST_SINGLE_TRANSACTION, this::getHighestSingleTransaction, this::setHighestSingleTransaction, Long.class);
        registerField(FINAL_DESTINATION, this::getFinalDestination, this::setFinalDestination, String.class);
        registerField(MARKET_PROFITS, this::getMarketProfits, this::setMarketProfits, Long.class);
        registerField(CURRENT_SHIP, this::getCurrentShip, this::setCurrentShip, String.class);
        registerField(TOTAL_BOUNTY_PROFIT, this::getTotalBountyProfit, this::setTotalBountyProfit, Long.class);
        registerField(PLAYER_MISSION_STATEMENT, this::getPlayerMissionStatement, this::setPlayerMissionStatement, String.class);
        registerField(CREW_WAGS_PAYOUT, this::getCrewWagsPayout, this::setCrewWagsPayout, Long.class);
        registerField(PLAYER_CUSTOM_TITLE, this::getPlayerTitle, this::setPlayerTitle, String.class);
        registerField(CURRENT_SHIP_NAME, this::getCurrentShipName, this::setCurrentShipName, String.class);
        registerField(PERSONAL_CREDITS_AVAILABLE, this::getPersonalCreditsAvailable, this::setPersonalCreditsAvailable, Long.class);
        registerField(SHIPS_OWNED, this::getShipsOwned, this::setShipsOwned, Integer.class);
        registerField(PLAYER_NAME, this::getPlayerName, this::setPlayerName, String.class);
        registerField(CARRIER_LOCATION, this::getLastKnownCarrierLocation, this::setLastKnownCarrierLocation, String.class);
        registerField(SHIP_FUEL_LEVEL, this::getShipFuelLevel, this::setShipFuelLevel, Double.class);
        registerField(FRIENDS_STATUS, this::getFriendsStatus, v -> {
            friendsStatus.clear();
            friendsStatus.putAll(v);
        }, new TypeToken<Map<String, String>>() {}.getType());
        registerField(CARRIER_DEPARTURE_TIME, this::getCarrierDepartureTime, this::setCarrierDepartureTime, String.class);
        registerField(JUMPING_TO_STARSYSTEM, this::getJumpingToStarSystem, this::setJumpingToStarSystem, String.class);
        registerField(PLAYER_HIGHEST_MILITARY_RANK, this::getPlayerHighestMilitaryRank, this::setPlayerHighestMilitaryRank, String.class);
        registerField(SPECIES_FIRST_LOGGED, this::getSpeciesFirstLogged, this::setSpeciesFirstLogged, Integer.class);
        registerField(SHIP_CARGO_CAPACITY, this::getShipCargoCapacity, this::setShipCargoCapacity, Integer.class);
        registerField(TOTAL_SYSTEMS_VISITED, this::getTotalSystemsVisited, this::setTotalSystemsVisited, Integer.class);
        registerField(TOTAL_BOUNTY_CLAIMED, this::getTotalBountyClaimed, this::setTotalBountyClaimed, Integer.class);
        registerField(GOODS_SOLD_THIS_SESSION, this::getGoodsSoldThisSession, this::setGoodsSoldThisSession, Integer.class);
        registerField(TOTAL_DISTANCE_TRAVELED, this::getTotalDistanceTraveled, this::setTotalDistanceTraveled, Double.class);
        registerField(FSD_TARGET, this::getFsdTarget, this::setFsdTarget, String.class);
        registerField("radio_on_off", this::isRadioTransmissionOn, this::setRadioTransmissionOn, Boolean.class);
        registerField("navigation_vox_on_off", this::isNavigationAnnouncementOn, this::setNavigationAnnouncementOn, Boolean.class);
        registerField("mining_vox_on_off", this::isMiningAnnouncementOn, this::setMiningAnnouncementOn, Boolean.class);
        registerField("discovery_vox_on_off", this::isDiscoveryAnnouncementOn, this::setDiscoveryAnnouncementOn, Boolean.class);
        registerField("route_vox_on_off", this::isRouteAnnouncementOn, this::setRouteAnnouncementOn, Boolean.class);


        EventBusManager.register(this);
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
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
        bountyCollectedThisSession += totalReward;
        save();
    }

    public void addBounty(BountyDto bounty) {
        bounties.add(bounty);
        save();
    }

    public void removeBounty(BountyDto bounty) {
        bounties.remove(bounty);
    }

    public long getBountyCollectedThisSession() {
        return bountyCollectedThisSession;
    }

    private void setBountyCollectedThisSession(long value) {
        this.bountyCollectedThisSession = value;
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

    public void clearMissions() {
        missions.clear();
        save();
    }

    public void setNavRoute(Map<Integer, NavRouteDto> routeMap) {
        this.routeMap.clear();
        save();
        this.routeMap.putAll(routeMap);
        save();
    }

    private Map<Integer, NavRouteDto> getRoute() {
        return routeMap;
    }

    public List<NavRouteDto> getOrderedRoute() {
        if (routeMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<NavRouteDto> orderedRoute = new ArrayList<>(routeMap.values());
        orderedRoute.sort(Comparator.comparingInt(NavRouteDto::getLeg));
        return orderedRoute;
    }

    public void clearRoute() {
        routeMap.clear();
        save();
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
        shipScans.clear();
        markets.clear();
        save();
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

    @Subscribe
    public void onLoadGame(LoadGameEvent event) {
        loadSavedStateFromDisk();
    }

    @Subscribe
    public void onLoadSession(LoadSessionEvent event) {
        loadSavedStateFromDisk();
    }

    public void onBounty(BountyDto data) {
        bounties.add(data);
        save();
    }

    public Set<BountyDto> getBounties() {
        return bounties;
    }

    @Subscribe
    public void onMissionAccepted(MissionAcceptedEvent event) {
        addMission(new MissionDto(event));
        save();
    }

    @Subscribe
    public void onMissionCompleted(MissionCompletedEvent event) {
        removeMission(event.getMissionID());
        if (missions.isEmpty()) targetFactions.clear();
        save();
    }

    public void addTargetFaction(String faction) {
        targetFactions.add(faction);
        save();
    }

    public Set<String> getTargetFactions() {
        if (getMissions() == null || getMissions().isEmpty()) {
            targetFactions.clear();
        }
        return targetFactions;
    }

    public void saveLocation(LocationDto object) {
        locations.put(object.getBodyId(), object);
        save();
    }

    public void setLocations(Map<Long, LocationDto> locations) {
        this.locations.putAll(locations);
        save();
    }

    public Map<Long, LocationDto> getLocations() {
        return locations;
    }

    public LocationDto getLocation(long id) {
        return locations.get(id) == null ? new LocationDto(id) : locations.get(id);
    }

    public void clearBounties() {
        bountyCollectedThisSession = 0;
        setTotalBountyProfit(0);
        setTotalBountyClaimed(0);
        bounties.clear();
        save();
    }

    public LocationDto getCurrentLocation() {
        return getLocation(currentLocationId == null ? 0 : currentLocationId);
    }

    public void saveCurrentLocation(LocationDto location) {
        saveLocation(location);
        setCurrentLocationId(location.getBodyId());
        save();
    }

    private void setCurrentLocationId(long id) {
        currentLocationId = id;
    }

    private Long getCurrentLocationId() {
        return currentLocationId;
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

    public void setHomeSystem(LocationDto currentLocation) {
        homeSystem = currentLocation;
        save();
    }

    public LocationDto getHomeSystem() {
        return homeSystem;
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
        this.bountyCollectedThisSession = 0;
        this.carrierData = new CarrierDataDto();
        this.loadout = null;
        this.markets.clear();
        this.missions.clear();
        this.rankAndProgressDto = new RankAndProgressDto();
        this.setShipLoadout(null);
        this.shipScans.clear();
        this.locations.clear();
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
        this.lastScanId= lastScan.getBodyId();
        save();
    }

    public LocationDto getLastScan() {
        return getLocation(lastScanId);
    }

    public TargetLocation getTracking() {
        return tracking == null ? new TargetLocation() : tracking;
    }

    public void setTracking(TargetLocation tracking) {
        this.tracking = tracking;
        save();
    }

    // New getters and setters
    public long getTotalHyperspaceDistance() {
        return totalHyperspaceDistance;
    }

    public void setTotalHyperspaceDistance(long totalHyperspaceDistance) {
        this.totalHyperspaceDistance = totalHyperspaceDistance;
        save();
    }

    public int getInsuranceClaims() {
        return insuranceClaims;
    }

    public void setInsuranceClaims(int insuranceClaims) {
        this.insuranceClaims = insuranceClaims;
        save();
    }

    public long getTotalProfitsFromExploration() {
        return totalProfitsFromExploration;
    }

    public void setTotalProfitsFromExploration(long totalProfitsFromExploration) {
        this.totalProfitsFromExploration = totalProfitsFromExploration;
        save();
    }

    public long getExobiologyProfits() {
        return exobiologyProfits;
    }

    public void setExobiologyProfits(long exobiologyProfits) {
        this.exobiologyProfits = exobiologyProfits;
        save();
    }

    public long getHighestSingleTransaction() {
        return highestSingleTransaction;
    }

    public void setHighestSingleTransaction(long highestSingleTransaction) {
        this.highestSingleTransaction = highestSingleTransaction;
        save();
    }

    public String getFinalDestination() {
        return finalDestination;
    }

    public void setFinalDestination(String finalDestination) {
        this.finalDestination = finalDestination;
        save();
    }

    public long getMarketProfits() {
        return marketProfits;
    }

    public void setMarketProfits(long marketProfits) {
        this.marketProfits = marketProfits;
        save();
    }

    public String getCurrentShip() {
        return currentShip;
    }

    public void setCurrentShip(String currentShip) {
        this.currentShip = currentShip;
        save();
    }

    public long getTotalBountyProfit() {
        return totalBountyProfit;
    }

    public void setTotalBountyProfit(long totalBountyProfit) {
        this.totalBountyProfit = totalBountyProfit;
        save();
    }

    public String getPlayerMissionStatement() {
        return playerMissionStatement;
    }

    public void setPlayerMissionStatement(String playerMissionStatement) {
        this.playerMissionStatement = playerMissionStatement;
        save();
    }

    public long getCrewWagsPayout() {
        return crewWagsPayout;
    }

    public void setCrewWagsPayout(long crewWagsPayout) {
        this.crewWagsPayout = crewWagsPayout;
        save();
    }

    public String getPlayerTitle() {
        return playerTitle;
    }

    public void setPlayerTitle(String playerTitle) {
        this.playerTitle = playerTitle;
        save();
    }

    public String getCurrentShipName() {
        return currentShipName;
    }

    public void setCurrentShipName(String currentShipName) {
        this.currentShipName = currentShipName;
        save();
    }

    public long getPersonalCreditsAvailable() {
        return personalCreditsAvailable;
    }

    public void setPersonalCreditsAvailable(long personalCreditsAvailable) {
        this.personalCreditsAvailable = personalCreditsAvailable;
        save();
    }

    public int getShipsOwned() {
        return shipsOwned;
    }

    public void setShipsOwned(int shipsOwned) {
        this.shipsOwned = shipsOwned;
        save();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        save();
    }

    public String getLastKnownCarrierLocation() {
        return lastKnownCarrierLocation;
    }

    public void setLastKnownCarrierLocation(String lastKnownCarrierLocation) {
        this.lastKnownCarrierLocation = lastKnownCarrierLocation;
        save();
    }

    public double getShipFuelLevel() {
        return shipFuelLevel;
    }

    public void setShipFuelLevel(double shipFuelLevel) {
        this.shipFuelLevel = shipFuelLevel;
        save();
    }

    public Map<String, String> getFriendsStatus() {
        return friendsStatus;
    }

    public void setFriendsStatus(Map<String, String> friendsStatus) {
        this.friendsStatus.clear();
        this.friendsStatus.putAll(friendsStatus);
        save();
    }

    public String getCarrierDepartureTime() {
        return carrierDepartureTime;
    }

    public void setCarrierDepartureTime(String carrierDepartureTime) {
        this.carrierDepartureTime = carrierDepartureTime;
        save();
    }

    public String getJumpingToStarSystem() {
        return jumpingToStarSystem;
    }

    public void setJumpingToStarSystem(String jumpingToStarSystem) {
        this.jumpingToStarSystem = jumpingToStarSystem;
        save();
    }

    public String getPlayerHighestMilitaryRank() {
        return playerHighestMilitaryRank;
    }

    public void setPlayerHighestMilitaryRank(String playerHighestMilitaryRank) {
        this.playerHighestMilitaryRank = playerHighestMilitaryRank;
        save();
    }

    public int getSpeciesFirstLogged() {
        return speciesFirstLogged;
    }

    public void setSpeciesFirstLogged(int speciesFirstLogged) {
        this.speciesFirstLogged = speciesFirstLogged;
        save();
    }

    public int getShipCargoCapacity() {
        return shipCargoCapacity;
    }

    public void setShipCargoCapacity(int shipCargoCapacity) {
        this.shipCargoCapacity = shipCargoCapacity;
        save();
    }

    public int getTotalSystemsVisited() {
        return totalSystemsVisited;
    }

    public void setTotalSystemsVisited(int totalSystemsVisited) {
        this.totalSystemsVisited = totalSystemsVisited;
        save();
    }

    public int getTotalBountyClaimed() {
        return totalBountyClaimed;
    }

    public void setTotalBountyClaimed(int totalBountyClaimed) {
        this.totalBountyClaimed = totalBountyClaimed;
        save();
    }

    public int getGoodsSoldThisSession() {
        return goodsSoldThisSession;
    }

    public void setGoodsSoldThisSession(int goodsSoldThisSession) {
        this.goodsSoldThisSession = goodsSoldThisSession;
        save();
    }

    public double getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    public void setTotalDistanceTraveled(double totalDistanceTraveled) {
        this.totalDistanceTraveled = totalDistanceTraveled;
        save();
    }

    public void setFsdTarget(String json) {
        this.fsdTarget = json;
        save();
    }

    public String getFsdTarget() {
        return fsdTarget;
    }

    public Boolean isRadioTransmissionOn() {
        return this.isRadioTransmissionOn;
    }
    public void setRadioTransmissionOn(Boolean radioTransmissionOn) {
        this.isRadioTransmissionOn = radioTransmissionOn;
        save();
    }

    public void clearLocations() {
        locations.clear();
        save();
    }


    public Boolean isMiningAnnouncementOn() {
        return isMiningAnnouncementOn == null || isMiningAnnouncementOn;
    }

    public void setMiningAnnouncementOn(Boolean miningAnnouncementOn) {
        isMiningAnnouncementOn = miningAnnouncementOn;
    }

    public Boolean isNavigationAnnouncementOn() {
        return isNavigationAnnouncementOn == null || isNavigationAnnouncementOn;
    }

    public void setNavigationAnnouncementOn(Boolean navigationAnnouncementOn) {
        isNavigationAnnouncementOn = navigationAnnouncementOn;
    }

    public Boolean isDiscoveryAnnouncementOn() {
        return isDiscoveryAnnouncementOn == null || isDiscoveryAnnouncementOn;
    }

    public void setDiscoveryAnnouncementOn(Boolean discoveryAnnouncementOn) {
        isDiscoveryAnnouncementOn = discoveryAnnouncementOn;
    }


    public Boolean isRouteAnnouncementOn() {
        return isRouteAnnouncementOn == null || isRouteAnnouncementOn;
    }

    public void setRouteAnnouncementOn(Boolean routeAnnouncementOn) {
        isRouteAnnouncementOn = routeAnnouncementOn;
    }

    public GalacticCoordinates getGalacticCoordinates() {
        GalacticCoordinates result;
        Map<Long, LocationDto> locations = getLocations();
        for(LocationDto location : locations.values()){
            if(location.getLocationType().equals(LocationDto.LocationType.PRIMARY_STAR)){
                return new GalacticCoordinates(location.getX(), location.getY(), location.getZ());
            }
        }
        return null;
    }

    public record GalacticCoordinates(double x, double y, double z){}

}
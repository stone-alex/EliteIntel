package elite.intel.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.reflect.TypeToken;
import elite.intel.ai.search.spansh.market.StationMarket;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.events.NavRouteDto;
import elite.intel.gameapi.journal.events.*;
import elite.intel.gameapi.journal.events.dto.*;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.*;


/**
 * The PlayerSession class manages the game session data for a player.
 * It is implemented as a singleton to ensure consistent access across the application.
 * The class handles persistence of session data, including player's progress,
 * gameplay metrics, and interactions within the game world.
 */
public class PlayerSession {
    private static final PlayerSession INSTANCE = new PlayerSession();
    private final SessionPersistence persistence = new SessionPersistence();

    ///
    public static final String CURRENT_SYSTEM_NAME = "current_system";
    public static final String SHIP_LOADOUT_JSON = "ship_loadout_json";
    public static final String SUITE_LOADOUT_JSON = "suite_loadout_json";
    public static final String FINAL_DESTINATION = "final_destination";
    public static final String CURRENT_FUEL_STATUS = "current_status";
    public static final String FSD_TARGET = "fsd_target";
    public static final String SHIP_CARGO = "ship_cargo";
    public static final String MISSIONS = "player_missions";
    public static final String REPUTATION = "reputation";
    public static final String PROFILE = "profile";
    public static final String PERSONALITY = "personality";
    public static final String JUMPING_TO = "jumping_to_starsystem";
    public static final String MATERIAL = "materials";
    public static final String LOCAL_MARKET_JSON = "local_market_json";
    public static final String LOCAL_OUTFITING_JSON = "local_outfiting_json";
    public static final String LOCAL_SHIP_YARD_JSON = "local_shipyard_json";
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
    public static final String LAST_SCAN = "last_scan";
    public static final String PLAYER_NAME = "player_name";
    public static final String PLAYER_MISSION_STATEMENT = "player_mission_statement";
    public static final String CURRENT_SHIP = "current_ship";
    public static final String CURRENT_SHIP_NAME = "current_ship_name";
    public static final String PERSONAL_CREDITS_AVAILABLE = "personal_credits_available";
    public static final String ROUTE_MAP = "routeMap";
    public static final String SHIP_SCANS = "shipScans";
    public static final String TARGET_FACTIONS = "targetFactions";
    public static final String STELLAR_OBJECTS = "stellarObjects";
    public static final String PLAYER_STATUS = "player_status";
    public static final String BIO_SAMPLES = "bio_samples";

    ///
    private static final String CURRENT_LOCATION = "current_location";
    private static final String FRIENDS_STATUS = "friends_status";
    private static final String CARRIER_STATS = "carrier_stats";
    private static final String BOUNTIES = "bounties";
    private static final String MINING_TARGETS = "miningTargets";
    private static final String HOME_SYSTEM = "home_system";

    ///
    private final Map<String, Object> state = new HashMap<>();
    private final Map<String, String> shipScans = new HashMap<>();
    private final Map<Long, MissionDto> missions = new LinkedHashMap<>();
    private final Map<String, StellarObjectDto> stellarObjects = new HashMap<>();
    private final Map<Integer, NavRouteDto> routeMap = new LinkedHashMap<>();
    private final Set<String> targetFactions = new LinkedHashSet<>();
    private final Set<BountyDto> bounties = new LinkedHashSet<>();
    private final Set<String> miningTargets = new HashSet<>();
    private List<StationMarket> markets = new ArrayList<>();
    private StatusDto status = new StatusDto();
    private long bountyCollectedThisSession = 0;
    private RankAndProgressDto rankAndProgressDto = new RankAndProgressDto();
    private LocationDto currentLocation = new LocationDto();
    private LocationDto homeSystem = new LocationDto();
    private CarrierDataDto carrierData = new CarrierDataDto();
    private List<BioSampleDto> bioSamples = new ArrayList<>();


    private PlayerSession() {
        persistence.ensureFileAndDirectoryExist("player_session.json");
        loadSavedStateFromDisk();
        state.put(FRIENDS_STATUS, new HashMap<String, String>());
        persistence.registerField(SHIP_SCANS, this::getShipScans, v -> {
            shipScans.clear();
            shipScans.putAll((Map<String, String>) v);
        }, new TypeToken<Map<String, String>>() {
        }.getType());
        persistence.registerField(MISSIONS, this::getMissions, v -> {
            missions.clear();
            missions.putAll((Map<Long, MissionDto>) v);
        }, new TypeToken<Map<Long, MissionDto>>() {
        }.getType());
        persistence.registerField(ROUTE_MAP, this::getRoute, v -> {
            routeMap.clear();
            routeMap.putAll((Map<Integer, NavRouteDto>) v);
        }, new TypeToken<Map<String, NavRouteDto>>() {
        }.getType());

        persistence.registerField(TARGET_FACTIONS, this::getTargetFactions, v -> {
            targetFactions.clear();
            targetFactions.addAll((Set<String>) v);
        }, new TypeToken<Set<String>>() {
        }.getType());

        persistence.registerField(BOUNTIES, this::getBounties, v -> {
            bounties.clear();
            bounties.addAll((Set<BountyDto>) v);
        }, new TypeToken<Set<BountyDto>>() {
        }.getType());

        persistence.registerField(STELLAR_OBJECTS, this::getStellarObjects, v -> {
            stellarObjects.clear();
            stellarObjects.putAll((Map<String, StellarObjectDto>) v);
        }, new TypeToken<Map<String, StellarObjectDto>>() {
        }.getType());

        persistence.registerField(MINING_TARGETS, this::getMiningTargets, v -> {
            miningTargets.clear();
            miningTargets.addAll((Set<String>) v);
        }, new TypeToken<Set<String>>() {}.getType());

        persistence.registerField("markets", this::getMarkets, this::setMarkets, new TypeToken<List<StationMarket>>(){}.getType());
        persistence.registerField(BIO_SAMPLES, this::getBioSamples, this::setBioSamples, new TypeToken<List<BioSampleDto>>(){}.getType() );

        persistence.registerField(CURRENT_LOCATION, this::getCurrentLocation, this::setCurrentLocation, LocationDto.class);
        persistence.registerField(HOME_SYSTEM, this::getHomeSystem, this::setHomeSystem, LocationDto.class);
        persistence.registerField(PLAYER_STATUS, this::getStatus, this::setStatus, StatusDto.class);


        persistence.registerField("bountyCollectedThisSession", this::getBountyCollectedThisSession, this::setBountyCollectedThisSession, Long.class);
        persistence.registerField("rankAndProgressDto", this::getRankAndProgressDto, this::setRankAndProgressDto, RankAndProgressDto.class);
        persistence.registerField(CARRIER_STATS, this::getCarrierData, this::setCarrierData, CarrierDataDto.class);
        EventBusManager.register(this);
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveSession));
    }

    public static PlayerSession getInstance() {
        return INSTANCE;
    }

    public void saveSession() {
        persistence.saveSession(state);
    }

    private void loadSavedStateFromDisk() {
        persistence.loadSession(json -> persistence.loadFields(json, state));
    }

    public void putShipScan(String shipName, String scan) {
        shipScans.put(shipName, scan);
        saveSession();
    }

    public String getShipScan(String shipName) {
        return shipScans.get(shipName);
    }

    private Map<String, String> getShipScans() {
        return shipScans;
    }

    public void put(String key, Object data) {
        state.put(key, data);
        saveSession();
    }

    public Object get(String key) {
        return state.get(key);
    }

    public void remove(String key) {
        state.remove(key);
        saveSession();
    }

    public void addBountyReward(long totalReward) {
        bountyCollectedThisSession += totalReward;
        saveSession();
    }
    public void addBounty(BountyDto bounty) {
        bounties.add(bounty);
        saveSession();
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
        saveSession();
    }

    public Map<Long, MissionDto> getMissions() {
        return missions;
    }

    public void removeMission(Long missionId) {
        missions.remove(missionId);
        saveSession();
    }

    public MissionDto getMission(Long missionId) {
        return missions.get(missionId);
    }

    public void clearMissions() {
        missions.clear();
        saveSession();
    }


    public void setNavRoute(Map<Integer, NavRouteDto> routeMap) {
        this.routeMap.clear();
        saveSession();
        this.routeMap.putAll(routeMap);
        saveSession();
    }

    public void removeNavPoint(String systemName) {
        Collection<NavRouteDto> values = routeMap.values();
        for (NavRouteDto navRouteDto : values) {
            if(navRouteDto.getName().equalsIgnoreCase(systemName)) {
                routeMap.remove(navRouteDto.getLeg());
            }
        }
        saveSession();
    }

    //for persistence
    private Map<Integer, NavRouteDto> getRoute() {
        return routeMap;
    }

    public List<NavRouteDto> getOrderedRoute() {
        if(routeMap.isEmpty()) {return new ArrayList<>();}
        List<NavRouteDto> orderedRoute = new ArrayList<>(routeMap.values());
        orderedRoute.sort(Comparator.comparingInt(NavRouteDto::getLeg));
        return orderedRoute;
    }

    public void clearRoute() {
        routeMap.clear();
        saveSession();
    }

    public RankAndProgressDto getRankAndProgressDto() {
        return rankAndProgressDto;
    }

    public void setRankAndProgressDto(RankAndProgressDto rankAndProgressDto) {
        this.rankAndProgressDto = rankAndProgressDto;
        saveSession();
    }

    public void clearShipScans() {
        shipScans.clear();
        saveSession();
    }

    public void clearOnShutDown() {
        shipScans.clear();
        markets.clear();
        saveSession();
    }


    public void onCarrierStats(CarrierStatsEvent event) {
        CarrierStatsEvent.Finance finance = event.getFinance();
        CarrierDataDto carrierData = new CarrierDataDto();
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
        saveSession();
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
        saveSession();
    }

    public Set<BountyDto> getBounties() {
        return bounties;
    }

    @Subscribe
    public void onMissionAccepted(MissionAcceptedEvent event) {
        addMission(new MissionDto(event));
        saveSession();
    }

    @Subscribe
    public void onMissionCompleted(MissionCompletedEvent event) {
        removeMission(event.getMissionID());
        if (missions.isEmpty()) targetFactions.clear();
        saveSession();
    }


    public void addTargetFaction(String faction) {
        targetFactions.add(faction);
        saveSession();
    }

    public Set<String> getTargetFactions() {
        if (getMissions() == null || getMissions().isEmpty()) {
            targetFactions.clear();
        }
        return targetFactions;
    }

    public void addStellarObject(StellarObjectDto object) {
        stellarObjects.put(object.getName(), object);
        saveSession();
    }

    public Map<String, StellarObjectDto> getStellarObjects() {
        return stellarObjects;
    }

    public StellarObjectDto getStellarObject(String name) {
        return stellarObjects.get(name);
    }

    public void clearStellarObjects() {
        stellarObjects.clear();
        saveSession();
    }


    public void clearBounties() {
        bountyCollectedThisSession = 0;
        state.remove(TOTAL_BOUNTY_PROFIT);
        state.remove(TOTAL_BOUNTY_CLAIMED);
        bounties.clear();
        saveSession();
    }

    public LocationDto getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LocationDto currentLocation) {
        this.currentLocation = currentLocation;
        saveSession();
    }

    public CarrierDataDto getCarrierData() {
        return carrierData;
    }

    public void setCarrierData(CarrierDataDto carrierData) {
        this.carrierData = carrierData;
    }

    public Set<String> getMiningTargets() {
        return miningTargets;
    }

    public void addMiningTarget(String miningTarget) {
        if( miningTarget == null || miningTarget.isEmpty()) return;
        miningTargets.add(miningTarget.toLowerCase());
        saveSession();
    }

    public void clearMiningTargets() {
        miningTargets.clear();
        saveSession();
    }

    public void setHomeSystem(LocationDto currentLocation) {
        homeSystem = currentLocation;
        saveSession();
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

    public StatusDto getStatus() {
        return status;
    }

    public void setStatus(StatusDto status) {
        this.status = status;
        saveSession();
    }

    public List<BioSampleDto> getBioSamples() {
        return bioSamples;
    }

    public void setBioSamples(List<BioSampleDto> bioSamples) {
        this.bioSamples = bioSamples;
        saveSession();
    }

    public void addBioSample(BioSampleDto bioSampleDto) {
        this.bioSamples.add(bioSampleDto);
        saveSession();
    }

    public void clearBioSamples() {
        this.bioSamples.clear();
        saveSession();
    }
}
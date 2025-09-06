package elite.companion.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.reflect.TypeToken;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.gameapi.journal.events.*;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.gameapi.journal.events.dto.MissionKillDto;
import elite.companion.gameapi.journal.events.dto.RankAndProgressDto;
import elite.companion.gameapi.journal.events.dto.StellarObjectDto;
import elite.companion.util.json.GsonFactory;

import java.util.*;

/**
 * The PlayerSession class manages and stores information related to the player's current session
 * in the game. This includes details about the player’s actions, status, missions, bounties,
 * ship scans, navigation routes, and other session-specific data. The class is designed
 * to persist session state, enable data retrieval, update session information, and handle
 * game-specific events.
 * <p>
 * Fields:
 * - CURRENT_SYSTEM: Tracks the player's current star system.
 * - SHIP_LOADOUT_JSON: JSON representation of the player's ship loadout.
 * - SUITE_LOADOUT_JSON: JSON representation of the player's suit loadout.
 * - FINAL_DESTINATION: Represents the player’s final destination.
 * - CURRENT_STATUS: Indicates the current in-game status of the player.
 * - FSD_TARGET: Stores details about the current frameshift drive target.
 * - SHIP_CARGO: Holds information about the cargo stored on the player's ship.
 * - CURRENT_SYSTEM_DATA: Stores detailed data about the player's current system.
 * - MISSIONS: Maintains a map of the player’s active missions.
 * - BOUNTIES: Records the bounty claims made by the player.
 * - REPUTATION: Tracks the player’s reputation across factions.
 * - CARRIER_LOCATION: Details the carrier's current location.
 * - CURRENT_LOCATION: Records the player's physical in-game location.
 * - PROFILE: Stores player profile-specific data.
 * - PERSONALITY: Represents traits or metadata about the player's gameplay style.
 * - JUMPING_TO: Indicates the system the player is jumping to (if in transit).
 * - MATERIALS: Tracks materials collected by the player.
 * - ENGINEER_PROGRESS: Details the player's progress with engineering unlocks.
 * - FRIENDS_STATUS: Tracks the player's friends list activity and connection status.
 * - SHIP_FUEL_LEVEL: Indicates the amount of fuel in the player’s ship.
 * - INSURANCE_CLAIMS: Tracks the number of ship insurance claims made by the player.
 * - SHIPS_OWNED: The total number of ships owned by the player.
 * - TOTAL_BOUNTY_CLAIMED: The cumulative bounty claimed by the player.
 * - TOTAL_BOUNTY_PROFIT: The total profit from bounty hunting operations.
 * - TOTAL_DISTANCE_TRAVELED: The total distance the player has traveled in the game.
 * - TOTAL_SYSTEMS_VISITED: The total number of star systems the player has visited.
 * - TOTAL_HYPERSPACE_DISTANCE: Tracks the hyperspace distance traveled.
 * - TOTAL_PROFITS_FROM_EXPLORATION: The total profits earned through exploration activities.
 * - SPECIES_FIRST_LOGGED: A record of species first logged by the player.
 * - EXOBIOLOGY_PROFITS: Total profits from exobiology discoveries.
 * - GOODS_SOLD_THIS_SESSION: Tracks goods sold during the current session.
 * - HIGHEST_SINGLE_TRANSACTION: The highest value from any single transaction in the game.
 * - MARKET_PROFITS: Cumulative profits from market trade.
 * - CREW_WAGS_PAYOUT: Total payout to crew during the session.
 * - SHIP_CARGO_CAPACITY: The cargo capacity of the player's ship.
 * - PLAYER_TITLE: The player's title (e.g., rank or status).
 * - PLAYER_HIGHEST_MILITARY_RANK: The highest military rank achieved by the player.
 * - LAST_SCAN: Data from the last scan performed.
 * - CARRIER_BALANCE: The credit balance associated with the carrier.
 * - CARRIER_RESERVE: Reserve credits stored for carrier operations.
 * - PLAYER_NAME: The name of the player.
 * - PLAYER_MISSION_STATEMENT: A user-defined mission statement or gameplay intention.
 * - CURRENT_SHIP: The identifier for the player's current ship.
 * - CURRENT_SHIP_NAME: The name of the ship currently in use.
 * - CARRIER_FUEL_LEVEL: Fuel level for the player-owned carrier.
 * - CARGO_SPACE_USED: Tracks how much of the cargo space has been used.
 * - PERSONAL_CREDITS_AVAILABLE: Credits available to the player.
 * - CARRIER_CALLSIGN: The carrier's unique identifier or callsign.
 * - CARRIER_NAME: The name of the player-owned carrier.
 * - CARRIER_TYPE: Specifies the type of the player-owned carrier.
 * - CARRIER_DOCKING_ACCESS: Determines docking access permissions for the carrier.
 * - CARRIER_CURRENT_JUMP_RANGE: Current available jump range of the carrier.
 * - CARRIER_MAX_JUMP_RANGE: Maximum jump range of the carrier.
 * - CARRIER_ALLOWS_NOTORIOUS_ACCESS: Indicates if the carrier allows access to notorious players.
 * - CARRIER_PENDING_DECOMMISSION: Flags whether the carrier is pending decommission.
 * - CARRIER_TOTAL_CARGO_SPACE_USED: Total cargo space of the carrier currently in use.
 * - CARRIER_CARGO_SPACE_RESERVED: Reserved cargo space on the carrier.
 * - CARRIER_SHIP_PACKS: List of available ship packs on the carrier.
 * - CARRIER_MODULE_PACKS: List of available module packs on the carrier.
 * - CARRIER_FREE_SPACE: Total free space available on the carrier.
 * - CARRIER_TOTAL_CAPACITY: Total capacity of the carrier.
 * - CARRIER_ALLOCATED_MARKET_BALANCE: Allocated credits for the carrier market.
 * - CARRIER_PIONEER_SUPPLY_TAX: Tax for pioneer supplies provided by the carrier.
 * - CARRIER_SHIPYARD_SUPPLY_TAX: Tax for shipyard supplies provided by the carrier.
 * - CARRIER_REARM_SUPPLY_TAX: Tax for rearm supplies provided by the carrier.
 * - CARRIER_REFUEL_SUPPLY_TAX: Tax for refuel supplies provided by the carrier.
 * - CARRIER_REPAIR_SUPPLY_TAX: Tax for repair supplies provided by the carrier.
 * - CARRIER_STATS: Holds detailed carrier statistics.
 * - INSTANCE: Singleton instance of PlayerSession.
 * - state: State management object tracking session parameters.
 * - shipScans: A map of ships scanned during the session.
 * - missions: A map of active missions during the session.
 * - missionKills: A set of mission-related kills tracked.
 * - stellarObjects: A map of celestial objects encountered.
 * - routeMap: Navigation route information.
 * - detectedSignals: A set of signals detected by the player.
 * - targetFactions: A set of target factions identified by the player.
 * - bountyCollectedThisSession: Tracks bounties collected during the session.
 * - rankAndProgressDto: Data transfer object representing rank and progress information.
 * - persistence: Handles persistence of session data.
 * <p>
 * Methods:
 * - getInstance(): Provides the singleton instance of the PlayerSession.
 * - saveSession(): Persists the current session state to disk.
 * - putShipScan(shipName, scan): Adds a ship scan record for the given ship.
 * - getShipScan(shipName): Retrieves the scan data for a specific ship.
 * - put(key, data): Adds arbitrary session-related data to the session store.
 * - get(key): Retrieves data from the session store.
 * - remove(key): Removes a specific piece of session data using its key.
 * - addBounty(totalReward): Adds the bounty amount to the session’s total.
 * - getBountyCollectedThisSession(): Returns the total bounties collected this session.
 * - addMission(mission): Adds a mission to the session.
 * - getMissions(): Retrieves all active missions.
 * - removeMission(missionId): Removes a mission by its ID.
 * - getMission(missionId): Retrieves a mission by its ID.
 * - clearMissions(): Removes all active missions.
 * - addBounty(bounty): Processes a bounty event and adds it to the session.
 * - getMissionsJson(): Returns mission data in JSON format.
 * - getBountiesJson(): Returns collected bounties in JSON format.
 * - getMissionKillsJson(): Returns mission kills in JSON format.
 * - setNavRoute(route): Sets the navigation route for the session.
 * - removeNavPoint(systemName): Removes a specific navigation point.
 * - getRoute(): Retrieves the entire navigation route.
 * - clearRoute(): Clears the navigation route.
 * - getRouteMapJson(): Returns navigation route information in JSON format.
 * - getRankAndProgressDto(): Retrieves player rank and progress information.
 * - setRankAndProgressDto(rankAndProgress): Sets data related to player rank and progress.
 * - clearShipScans(): Removes all stored ship scans.
 * - clearOnShutDown(): Clears session data upon shutdown.
 * - addSignal(event): Processes a signal event during the session.
 * - getSignals(): Retrieves signals detected during the session.
 * - clearFssSignals(): Clears the full-spectrum system (FSS) scans.
 * - addMissionKill(missionKill): Adds a mission-related kill to the session.
 * - getMissionKills(): Retrieves all mission-related kills.
 * - addTargetFaction(faction): Adds a faction as a target.
 * - getTargetFactions(): Retrieves all targeted factions.
 * - addStellarObject(object): Adds a stellar object to the session.
 * - getStellarObjects(): Retrieves all stored stellar object data.
 * - getStellarObject(name): Retrieves a specific stellar object by name.
 * - clearStellarObjects(): Removes all stored stellar objects.
 * - clearBounties(): Resets all bounty-related data.
 */
public class PlayerSession {

    public static final String CURRENT_SYSTEM = "current_system";
    public static final String SHIP_LOADOUT_JSON = "ship_loadout_json";
    public static final String SUITE_LOADOUT_JSON = "suite_loadout_json";
    public static final String FINAL_DESTINATION = "final_destination";
    public static final String CURRENT_STATUS = "current_status";
    public static final String FSD_TARGET = "fsd_target";
    public static final String SHIP_CARGO = "ship_cargo";
    public static final String CURRENT_SYSTEM_DATA = "current_system_data";
    public static final String MISSIONS = "player_missions";
    public static final String BOUNTIES = "bounties";
    public static final String REPUTATION = "reputation";
    public static final String CARRIER_LOCATION = "carrier_location";
    public static final String CURRENT_LOCATION = "current_location";
    public static final String PROFILE = "profile";
    public static final String PERSONALITY = "personality";
    public static final String JUMPING_TO = "jumping_to_starsystem";
    public static final String MATERIALS = "materials";
    public static final String ENGINEER_PROGRESS = "engineer_progress";
    private static final String FRIENDS_STATUS = "friends_status";

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
    public static final String PLAYER_TITLE = "player_title";
    public static final String PLAYER_HIGHEST_MILITARY_RANK = "player_highest_military_rank";
    public static final String LAST_SCAN = "last_scan";
    public static final String CARRIER_BALANCE = "carrier_balance";
    public static final String CARRIER_RESERVE = "carrier_reserve";
    public static final String PLAYER_NAME = "player_name";
    public static final String PLAYER_MISSION_STATEMENT = "player_mission_statement";
    public static final String CURRENT_SHIP = "current_ship";
    public static final String CURRENT_SHIP_NAME = "current_ship_name";
    public static final String CARRIER_FUEL_LEVEL = "carrier_fuel_level";
    public static final String CARGO_SPACE_USED = "cargo_space_used";
    public static final String PERSONAL_CREDITS_AVAILABLE = "personal_credits_available";
    public static final String CARRIER_CALLSIGN = "carrier_callsign";
    public static final String CARRIER_NAME = "carrier_name";
    public static final String CARRIER_TYPE = "carrier_type";
    public static final String CARRIER_DOCKING_ACCESS = "carrier_docking_access";
    public static final String CARRIER_CURRENT_JUMP_RANGE = "carrier_current_jump_range";
    public static final String CARRIER_MAX_JUMP_RANGE = "carrier_max_jump_range";
    public static final String CARRIER_ALLOWS_NOTORIOUS_ACCESS = "carrier_allows_notorious_access";
    public static final String CARRIER_PENDING_DECOMMISSION = "carrier_pending_decommission";
    public static final String CARRIER_TOTAL_CARGO_SPACE_USED = "carrier_total_cargo_space_used";
    public static final String CARRIER_CARGO_SPACE_RESERVED = "carrier_cargo_space_reserved";
    public static final String CARRIER_SHIP_PACKS = "carrier_ship_packs";
    public static final String CARRIER_MODULE_PACKS = "carrier_module_packs";
    public static final String CARRIER_FREE_SPACE = "carrier_free_space";
    public static final String CARRIER_TOTAL_CAPACITY = "carrier_total_capacity";
    public static final String CARRIER_ALLOCATED_MARKET_BALANCE = "carrier_allocated_market_balance";
    public static final String CARRIER_PIONEER_SUPPLY_TAX = "carrier_pioneer_supply_tax";
    public static final String CARRIER_SHIPYARD_SUPPLY_TAX = "carrier_shipyard_supply_tax";
    public static final String CARRIER_REARM_SUPPLY_TAX = "carrier_rearm_supply_tax";
    public static final String CARRIER_REFUEL_SUPPLY_TAX = "carrier_refuel_supply_tax";
    public static final String CARRIER_REPAIR_SUPPLY_TAX = "carrier_repair_supply_tax";
    public static final String CARRIER_STATS = "carrier_stats";

    private static final PlayerSession INSTANCE = new PlayerSession();
    private final Map<String, Object> state = new HashMap<>();
    private final Map<String, String> shipScans = new HashMap<>();
    private final Map<Long, MissionDto> missions = new LinkedHashMap<>();
    private final Set<MissionKillDto> missionKills = new LinkedHashSet<>();
    private final Map<String, StellarObjectDto> stellarObjects = new HashMap<>();
    private final Map<String, NavRouteDto> routeMap = new LinkedHashMap<>();
    private final Set<String> detectedSignals = new LinkedHashSet<>();
    private final Set<String> targetFactions = new LinkedHashSet<>();
    private long bountyCollectedThisSession = 0;
    private RankAndProgressDto rankAndProgressDto = new RankAndProgressDto();
    private final SessionPersistence persistence = new SessionPersistence("session/player_session.json");

    private Map<String, Object> getState() {
        return state;
    }

    private PlayerSession() {
        state.put(FRIENDS_STATUS, new HashMap<String, String>());
/*
        persistence.registerField("state", this::getState, v ->{
            state.clear();
            state.putAll((Map<String, Object>) v);
        }, new TypeToken<Map<String, Object>>(){}.getType());
*/
        persistence.registerField("shipScans", this::getShipScans, v -> {
            shipScans.clear();
            shipScans.putAll((Map<String, String>) v);
        }, new TypeToken<Map<String, String>>() {
        }.getType());
        persistence.registerField(MISSIONS, this::getMissions, v -> {
            missions.clear();
            missions.putAll((Map<Long, MissionDto>) v);
        }, new TypeToken<Map<Long, MissionDto>>() {
        }.getType());
        persistence.registerField("routeMap", this::getRoute, v -> {
            routeMap.clear();
            routeMap.putAll((Map<String, NavRouteDto>) v);
        }, new TypeToken<Map<String, NavRouteDto>>() {
        }.getType());
        persistence.registerField("detectedSignals", this::getDetectedSignals, v -> {
            detectedSignals.clear();
            detectedSignals.addAll((Set<String>) v);
        }, new TypeToken<Set<String>>() {
        }.getType());
        persistence.registerField("targetFactions", this::getTargetFactions, v -> {
            targetFactions.clear();
            targetFactions.addAll((Set<String>) v);
        }, new TypeToken<Set<String>>() {
        }.getType());
        persistence.registerField("missionKills", this::getMissionKills, v -> {
            missionKills.clear();
            missionKills.addAll((Set<MissionKillDto>) v);
        }, new TypeToken<Set<MissionDto>>() {
        }.getType());
        persistence.registerField("stellarObjects", this::getStellarObjects, v -> {
            stellarObjects.clear();
            stellarObjects.putAll((Map<String, StellarObjectDto>) v);
        }, new TypeToken<Map<String, StellarObjectDto>>() {
        }.getType());

        persistence.registerField("bountyCollectedThisSession", this::getBountyCollectedThisSession, this::setBountyCollectedThisSession, Long.class);
        persistence.registerField("rankAndProgressDto", this::getRankAndProgressDto, this::setRankAndProgressDto, RankAndProgressDto.class);
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

    public void addBounty(long totalReward) {
        bountyCollectedThisSession += totalReward;
        saveSession();
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


    public void addBounty(BountyEvent bounty) {
        List<BountyEvent> bounties = (List<BountyEvent>) state.computeIfAbsent(BOUNTIES, k -> new ArrayList<BountyEvent>());
        bounties.add(bounty);
        saveSession();
    }


    public String getMissionsJson() {
        return missions.isEmpty() ? "{}" : GsonFactory.getGson().toJson(missions);
    }

    public String getBountiesJson() {
        List<BountyEvent> bounties = (List<BountyEvent>) state.get(BOUNTIES);
        return bounties == null || bounties.isEmpty() ? "[]" : GsonFactory.getGson().toJson(bounties);
    }

    public String getMissionKillsJson() {
        return missionKills.isEmpty() ? "{}" : GsonFactory.getGson().toJson(missionKills);
    }

    public void setNavRoute(Map<String, NavRouteDto> routeMap) {
        this.routeMap.clear();
        this.routeMap.putAll(routeMap);
        saveSession();
    }

    public void removeNavPoint(String systemName) {
        routeMap.remove(systemName);
        saveSession();
    }

    public Map<String, NavRouteDto> getRoute() {
        return routeMap;
    }

    public void clearRoute() {
        routeMap.clear();
        saveSession();
    }

    public String getRouteMapJson() {
        return routeMap.isEmpty() ? "{}" : GsonFactory.getGson().toJson(routeMap);
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
        state.clear();
        shipScans.clear();
        missions.clear();
        missionKills.clear();
        routeMap.clear();
        targetFactions.clear();
        detectedSignals.clear();
        bountyCollectedThisSession = 0;
        rankAndProgressDto = new RankAndProgressDto();
        persistence.deleteSessionFile();
    }


    public void addSignal(BaseEvent event) {
        detectedSignals.add(event.toJson());
        saveSession();
    }

    public String getSignals() {
        Object[] array = detectedSignals.stream().toArray();
        StringBuilder sb = new StringBuilder("[");
        for (Object o : array) {
            sb.append(o).append(", ");
        }
        sb.append("]");
        return array.length == 0 ? "no data" : sb.toString();
    }

    public void clearFssSignals() {
        detectedSignals.clear();
        saveSession();
    }

    private Set<String> getDetectedSignals() {
        return detectedSignals;
    }

    @Subscribe
    public void onCarrierStats(CarrierStatsEvent event) {
        CarrierStatsEvent.Finance finance = event.getFinance();
        state.put(CARRIER_FUEL_LEVEL, event.getFuelLevel());
        state.put(CARGO_SPACE_USED, event.getSpaceUsage());
        state.put(CARRIER_CALLSIGN, event.getCallsign());
        state.put(CARRIER_NAME, event.getName());
        state.put(CARRIER_TYPE, event.getCarrierType());
        state.put(CARRIER_DOCKING_ACCESS, event.getDockingAccess());
        state.put(CARRIER_CURRENT_JUMP_RANGE, event.getJumpRangeCurr());
        state.put(CARRIER_MAX_JUMP_RANGE, event.getJumpRangeMax());
        state.put(CARRIER_ALLOWS_NOTORIOUS_ACCESS, event.isAllowNotorious());
        state.put(CARRIER_PENDING_DECOMMISSION, event.isPendingDecommission());
        if (event.getSpaceUsage() != null) {
            CarrierStatsEvent.SpaceUsage spaceUsage = event.getSpaceUsage();
            state.put(CARRIER_TOTAL_CARGO_SPACE_USED, spaceUsage.getCargo());
            state.put(CARRIER_CARGO_SPACE_RESERVED, spaceUsage.getCargoSpaceReserved());
            state.put(CARRIER_SHIP_PACKS, spaceUsage.getShipPacks());
            state.put(CARRIER_MODULE_PACKS, spaceUsage.getModulePacks());
            state.put(CARRIER_FREE_SPACE, spaceUsage.getFreeSpace());
            state.put(CARRIER_TOTAL_CAPACITY, spaceUsage.getTotalCapacity());
        }

        if (finance != null) {
            state.put(CARRIER_BALANCE, finance.getCarrierBalance());
            state.put(CARRIER_RESERVE, finance.getReserveBalance());
            state.put(CARRIER_ALLOCATED_MARKET_BALANCE, finance.getAvailableBalance());
            state.put(CARRIER_PIONEER_SUPPLY_TAX, finance.getTaxRate_pioneersupplies());
            state.put(CARRIER_SHIPYARD_SUPPLY_TAX, finance.getTaxRate_shipyard());
            state.put(CARRIER_REARM_SUPPLY_TAX, finance.getTaxRate_rearm());
            state.put(CARRIER_REFUEL_SUPPLY_TAX, finance.getTaxRate_refuel());
            state.put(CARRIER_REPAIR_SUPPLY_TAX, finance.getTaxRate_repair());

            long carrierBalance = finance.getCarrierBalance();
            long reserveBalance = finance.getReserveBalance();
            put(CARRIER_BALANCE, String.valueOf(carrierBalance));
            put(CARRIER_RESERVE, String.valueOf(reserveBalance));

            int jumps = event.getFuelLevel() / 90;
            put(CARRIER_STATS, "Credit balance: " + carrierBalance + " reserved balance: " + reserveBalance + " fuel level: " + event.getFuelLevel() + " enough for " + event.getFuelLevel() / 90 + " jumps or " + (jumps * 500) + " light years");
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

    @Subscribe
    public void onBounty(BountyEvent event) {
        addBounty(event);
        addBounty(event.getTotalReward());
        saveSession();
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

    public void addMissionKill(MissionKillDto missionKillDto) {
        missionKills.add(missionKillDto);
    }

    public Set<MissionKillDto> getMissionKills() {
        return missionKills;
    }

    public void addTargetFaction(String faction) {
        targetFactions.add(faction);
        saveSession();
    }

    public Set<String> getTargetFactions() {
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
        state.remove(BOUNTIES);
        state.remove(TOTAL_BOUNTY_PROFIT);
        state.remove(TOTAL_BOUNTY_CLAIMED);
    }
}
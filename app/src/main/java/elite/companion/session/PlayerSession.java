package elite.companion.session;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.gameapi.journal.events.*;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.gameapi.journal.events.dto.MissionKillDto;
import elite.companion.gameapi.journal.events.dto.RankAndProgressDto;
import elite.companion.util.EventBusManager;
import elite.companion.util.GsonFactory;

import java.util.*;

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
    private final Map<String, NavRouteDto> routeMap = new LinkedHashMap<>();
    private final Set<String> detectedSignals = new LinkedHashSet<>();
    private final Set<String> targetFactions = new LinkedHashSet<>();
    private long bountyCollectedThisSession = 0;
    private RankAndProgressDto rankAndProgressDto = new RankAndProgressDto();
    private final SessionPersistence persistence = new SessionPersistence("session/player_session.json");

    private PlayerSession() {
        state.put(FRIENDS_STATUS, new HashMap<String, String>());
        persistence.registerField("shipScans", this::getShipScans, v -> {
            shipScans.clear();
            shipScans.putAll((Map<String, String>) v);
        }, Map.class);
        persistence.registerField(MISSIONS, this::getMissions, v -> {
            missions.clear();
            missions.putAll((Map<Long, MissionDto>) v);
        }, Map.class);
        persistence.registerField("routeMap", this::getRoute, v -> {
            routeMap.clear();
            routeMap.putAll((Map<String, NavRouteDto>) v);
        }, Map.class);
        persistence.registerField("detectedSignals", this::getDetectedSignals, v -> {
            detectedSignals.clear();
            detectedSignals.addAll((Set<String>) v);
        }, Set.class);
        persistence.registerField("targetFactions", this::getTargetFactions, v -> {
            targetFactions.clear();
            targetFactions.addAll((Set<String>) v);
        }, Set.class);
        persistence.registerField("missionKills", this::getMissionKills, v -> {
            missionKills.clear();
            missionKills.addAll((Set<MissionKillDto>) v);
        }, Set.class);

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


    public void clearBounties() {
        bountyCollectedThisSession = 0;
        state.remove(BOUNTIES);
        state.remove(TOTAL_BOUNTY_PROFIT);
        state.remove(TOTAL_BOUNTY_CLAIMED);
    }
}
package elite.companion.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.gameapi.journal.events.*;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.gameapi.journal.events.dto.RankAndProgressDto;
import elite.companion.util.EventBusManager;
import elite.companion.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PlayerSession {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerSession.class);

    public static final String CURRENT_SYSTEM = "current_system";
    public static final String SHIP_LOADOUT_JSON = "ship_loadout_json";
    public static final String SUITE_LOADOUT_JSON = "suite_loadout_json";
    public static final String FINAL_DESTINATION = "final_destination";
    public static final String CURRENT_STATUS = "current_status";
    public static final String FSD_TARGET = "fsd_target";
    public static final String SHIP_CARGO = "ship_cargo";
    public static final String CURRENT_SYSTEM_DATA = "current_system_data";
    public static final String MISSIONS = "player_missions";
    public static final String PIRATE_BOUNTIES = "pirate_bounties";
    public static final String REPUTATION = "reputation";
    public static final String CARRIER_LOCATION = "carrier_location";
    public static final String CURRENT_LOCATION = "current_location";
    public static final String TARGET_FACTION_NAME = "target_faction_name";
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
    private final Map<String, NavRouteDto> routeMap = new LinkedHashMap<>();
    private long bountyCollectedThisSession = 0;
    private RankAndProgressDto rankAndProgressDto = new RankAndProgressDto();

    private static final String SESSION_FILE = "session/player_session.json";
    private static final Gson GSON = GsonFactory.getGson();

    private PlayerSession() {
        state.put(FRIENDS_STATUS, new HashMap<String, String>());
        EventBusManager.register(this);
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveSession));
    }

    public static PlayerSession getInstance() {
        return INSTANCE;
    }

    private File ensureSessionDirectory() {
        String root = System.getProperty("user.dir");
        File file = new File(root, SESSION_FILE);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                LOG.error("Failed to create session directory: {}", parentDir.getPath());
                return null;
            }
        }
        return file;
    }

    public void saveSession() {
        File file = ensureSessionDirectory();
        if (file == null) {
            return;
        }

        // Load existing session to merge with in-memory state
        JsonObject existingJson = loadSessionForMerge();

        // Prepare new JSON
        JsonObject json = new JsonObject();
        JsonObject stateJson = existingJson.has("state") ? existingJson.getAsJsonObject("state") : new JsonObject();
        // Update missions in state.player_missions
        stateJson.add(MISSIONS, GSON.toJsonTree(missions));
        // Update bounties in state.pirate_bounties
        stateJson.add(PIRATE_BOUNTIES, GSON.toJsonTree(state.get(PIRATE_BOUNTIES)));
        // Update other state entries, preserving existing ones
        for (Map.Entry<String, Object> entry : state.entrySet()) {
            if (!entry.getKey().equals(MISSIONS) && !entry.getKey().equals(PIRATE_BOUNTIES)) {
                stateJson.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
            }
        }
        json.add("state", stateJson);
        json.add("shipScans", GSON.toJsonTree(shipScans));
        json.add("routeMap", GSON.toJsonTree(routeMap));
        json.addProperty("bountyCollectedThisSession", bountyCollectedThisSession);
        json.add("rankAndProgressDto", GSON.toJsonTree(rankAndProgressDto));

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
            LOG.debug("Saved player session to: {}", file.getPath());
        } catch (IOException e) {
            LOG.error("Failed to save player session to {}: {}", file.getPath(), e.getMessage(), e);
        }
    }

    private JsonObject loadSessionForMerge() {
        File file = ensureSessionDirectory();
        if (file == null || !file.exists()) {
            return new JsonObject();
        }

        try (Reader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            LOG.error("Failed to load player session for merge from {}: {}", file.getPath(), e.getMessage(), e);
            return new JsonObject();
        }
    }

    private void loadSavedStateFromDisk() {
        File file = ensureSessionDirectory();
        if (file == null) {
            return;
        }

        if (!file.exists()) {
            try {
                JsonObject emptyJson = new JsonObject();
                emptyJson.add("state", new JsonObject());
                emptyJson.add("shipScans", new JsonObject());
                emptyJson.add("routeMap", new JsonObject());
                emptyJson.addProperty("bountyCollectedThisSession", 0);
                emptyJson.add("rankAndProgressDto", GSON.toJsonTree(new RankAndProgressDto()));
                Files.write(file.toPath(), GSON.toJson(emptyJson).getBytes());
                LOG.info("Created empty player session file: {}", file.getPath());
            } catch (IOException e) {
                LOG.error("Failed to create empty player session file {}: {}", file.getPath(), e.getMessage(), e);
                return;
            }
        }

        try (Reader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // Load state map
            if (json.has("state")) {
                JsonObject stateJson = json.getAsJsonObject("state");
                // Handle player_missions specifically
                if (stateJson.has(MISSIONS)) {
                    try {
                        Type missionMapType = new TypeToken<Map<Long, MissionDto>>() {}.getType();
                        Map<Long, MissionDto> loadedMissions = GSON.fromJson(stateJson.get(MISSIONS), missionMapType);
                        if (loadedMissions != null) {
                            missions.putAll(loadedMissions);
                            LOG.debug("Deserialized player_missions with {} entries", loadedMissions.size());
                        } else {
                            LOG.warn("player_missions JSON is null or empty");
                            missions.clear();
                        }
                    } catch (JsonSyntaxException | IllegalStateException e) {
                        LOG.error("Failed to deserialize player_missions from JSON: {}. Error: {}", stateJson.get(MISSIONS), e.getMessage(), e);
                        missions.clear();
                    }
                }
                // Load other state entries
                for (Map.Entry<String, JsonElement> entry : stateJson.entrySet()) {
                    if (!entry.getKey().equals(MISSIONS)) {
                        state.put(entry.getKey(), GSON.fromJson(entry.getValue(), Object.class));
                    }
                }
            }

            // Load ship scans
            if (json.has("shipScans")) {
                JsonObject scansJson = json.getAsJsonObject("shipScans");
                for (Map.Entry<String, JsonElement> entry : scansJson.entrySet()) {
                    shipScans.put(entry.getKey(), entry.getValue().getAsString());
                }
                LOG.debug("Deserialized {} ship scans", shipScans.size());
            }

            // Load route map
            if (json.has("routeMap") && !json.get("routeMap").isJsonObject()) {
                LOG.error("routeMap JSON is not an object: {}", json.get("routeMap"));
                routeMap.clear();
            } else if (json.has("routeMap")) {
                try {
                    Type routeMapType = new TypeToken<Map<String, NavRouteDto>>() {}.getType();
                    Map<String, NavRouteDto> loadedRouteMap = GSON.fromJson(json.get("routeMap"), routeMapType);
                    if (loadedRouteMap != null) {
                        for (Map.Entry<String, NavRouteDto> entry : loadedRouteMap.entrySet()) {
                            NavRouteDto route = entry.getValue();
                            if (route == null || route.getName() == null || route.getName().trim().isEmpty()) {
                                LOG.warn("Invalid NavRouteDto for system: {}. Skipping entry.", entry.getKey());
                                continue;
                            }
                            routeMap.put(entry.getKey(), route);
                        }
                        LOG.debug("Deserialized routeMap with {} valid entries", routeMap.size());
                    } else {
                        LOG.warn("routeMap JSON is null or empty");
                        routeMap.clear();
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    LOG.error("Failed to deserialize routeMap from JSON: {}. Error: {}", json.get("routeMap"), e.getMessage(), e);
                    routeMap.clear();
                }
            }

            // Load other fields
            if (json.has("bountyCollectedThisSession")) {
                bountyCollectedThisSession = json.get("bountyCollectedThisSession").getAsLong();
            }
            if (json.has("rankAndProgressDto")) {
                rankAndProgressDto = GSON.fromJson(json.get("rankAndProgressDto"), RankAndProgressDto.class);
            }
            LOG.debug("Loaded player session from: {}", file.getPath());
        } catch (IOException | JsonSyntaxException e) {
            LOG.error("Failed to load player session from {}: {}", file.getPath(), e.getMessage(), e);
        }
    }

    public void putShipScan(String shipName, String scan) {
        shipScans.put(shipName, scan);
        saveSession();
    }

    public String getShipScan(String shipName) {
        return shipScans.get(shipName);
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
        bountyCollectedThisSession = bountyCollectedThisSession + totalReward;
        saveSession();
    }

    public long getBountyCollectedThisSession() {
        return bountyCollectedThisSession;
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

    public void addPirateMission(MissionDto mission) {
        missions.put(mission.getMissionId(), mission);
        saveSession();
    }

    public void addPirateBounty(BountyEvent bounty) {
        List<BountyEvent> bounties = (List<BountyEvent>) state.computeIfAbsent(PIRATE_BOUNTIES, k -> new ArrayList<BountyEvent>());
        bounties.add(bounty);
        saveSession();
    }

    public void removePirateMission(long missionId) {
        missions.remove(missionId);
        saveSession();
    }

    public String getPirateMissionsJson() {
        return missions.isEmpty() ? "{}" : GSON.toJson(missions);
    }

    public String getPirateBountiesJson() {
        List<BountyEvent> bounties = (List<BountyEvent>) state.get(PIRATE_BOUNTIES);
        return bounties == null || bounties.isEmpty() ? "[]" : GSON.toJson(bounties);
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
        Map<String, NavRouteDto> routes = routeMap;
        return routes == null || routes.isEmpty() ? "{}" : GSON.toJson(routes);
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
        routeMap.clear();
        bountyCollectedThisSession = 0;
        rankAndProgressDto = new RankAndProgressDto();
        deleteSessionFile();
    }

    private void deleteSessionFile() {
        String root = System.getProperty("user.dir");
        File file = new File(root, SESSION_FILE);
        if (file.exists()) {
            try {
                Files.delete(Paths.get(file.getPath()));
                LOG.debug("Deleted player session file: {}", file.getPath());
            } catch (IOException e) {
                LOG.error("Failed to delete player session file {}: {}", file.getPath(), e.getMessage(), e);
            }
        }
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
        addPirateBounty(event);
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
}
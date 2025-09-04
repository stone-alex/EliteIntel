package elite.companion.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import elite.companion.comms.ai.AICadence;
import elite.companion.comms.ai.AIPersonality;
import elite.companion.comms.voice.Voices;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.gameapi.journal.events.*;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.util.EventBusManager;
import elite.companion.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SystemSession {
    private static final Logger LOG = LoggerFactory.getLogger(SystemSession.class);

    public static final String CURRENT_SYSTEM = "current_system";
    public static final String SHIP_LOADOUT_JSON = "ship_loadout_json";
    public static final String SUITE_LOADOUT_JSON = "suite_loadout_json";
    public static final String FINAL_DESTINATION = "final_destination";
    public static final String CURRENT_STATUS = "current_status";
    public static final String FSD_TARGET = "fsd_target";
    public static final String SHIP_CARGO = "ship_cargo";
    public static final String CURRENT_SYSTEM_DATA = "current_system_data";
    public static final String MISSIONS = "player_missions"; // Renamed from PIRATE_MISSIONS
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

    public static final String RADION_TRANSMISSION_ON_OFF = "radio_transmission_on_off";

    private static final SystemSession INSTANCE = new SystemSession();
    private final Map<String, Object> state = new HashMap<>();
    private final Set<String> detectedSignals = new LinkedHashSet<>();
    private final Map<Long, MissionDto> missions = new LinkedHashMap<>();
    private final Map<String, NavRouteDto> routeMap = new LinkedHashMap<>();
    private long bountyCollectedThisSession = 0;
    private Voices aiVoice;
    private AIPersonality aiPersonality;
    private AICadence aiCadence;
    private BaseEvent bodySignal;
    private JsonArray chatHistory = new JsonArray();
    private boolean isPrivacyModeOn = false;

    private static final String SESSION_FILE = "session/system_session.json";
    private static final Gson GSON = GsonFactory.getGson();

    private SystemSession() {
        state.put(FRIENDS_STATUS, new HashMap<String, String>());
        EventBusManager.register(this);
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveSession));
    }

    public static SystemSession getInstance() {
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
        // Update other state entries, preserving existing ones
        for (Map.Entry<String, Object> entry : state.entrySet()) {
            if (!entry.getKey().equals(MISSIONS)) {
                stateJson.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
            }
        }
        json.add("state", stateJson);

        // Preserve or update other fields
        json.add("detectedSignals", existingJson.has("detectedSignals") ? existingJson.get("detectedSignals") : GSON.toJsonTree(detectedSignals));
        json.add("routeMap", GSON.toJsonTree(routeMap));
        json.addProperty("bountyCollectedThisSession", bountyCollectedThisSession);
        json.addProperty("aiVoice", aiVoice != null ? aiVoice.name() : (existingJson.has("aiVoice") && !existingJson.get("aiVoice").isJsonNull() ? existingJson.get("aiVoice").getAsString() : null));
        json.addProperty("aiPersonality", aiPersonality != null ? aiPersonality.name() : (existingJson.has("aiPersonality") && !existingJson.get("aiPersonality").isJsonNull() ? existingJson.get("aiPersonality").getAsString() : null));
        json.addProperty("aiCadence", aiCadence != null ? aiCadence.name() : (existingJson.has("aiCadence") && !existingJson.get("aiCadence").isJsonNull() ? existingJson.get("aiCadence").getAsString() : null));
        json.add("bodySignal", bodySignal != null ? JsonParser.parseString(bodySignal.toJson()) : (existingJson.has("bodySignal") ? existingJson.get("bodySignal") : null));
        json.add("chatHistory", existingJson.has("chatHistory") ? existingJson.get("chatHistory") : chatHistory);

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
            LOG.debug("Saved system session to: {}", file.getPath());
        } catch (IOException e) {
            LOG.error("Failed to save system session to {}: {}", file.getPath(), e.getMessage(), e);
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
            LOG.error("Failed to load session for merge from {}: {}", file.getPath(), e.getMessage(), e);
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
                emptyJson.add("detectedSignals", new JsonArray());
                emptyJson.add("routeMap", new JsonObject());
                emptyJson.addProperty("bountyCollectedThisSession", 0);
                emptyJson.add("aiVoice", null);
                emptyJson.add("aiPersonality", null);
                emptyJson.add("aiCadence", null);
                emptyJson.add("bodySignal", null);
                emptyJson.add("chatHistory", new JsonArray());
                Files.write(file.toPath(), GSON.toJson(emptyJson).getBytes());
                LOG.info("Created empty system session file: {}", file.getPath());
            } catch (IOException e) {
                LOG.error("Failed to create empty system session file {}: {}", file.getPath(), e.getMessage(), e);
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
                // Handle legacy pirate_missions for migration
                if (stateJson.has("pirate_missions")) {
                    try {
                        Type missionListType = new TypeToken<List<MissionDto>>() {}.getType();
                        List<MissionDto> legacyMissions = GSON.fromJson(stateJson.get("pirate_missions"), missionListType);
                        if (legacyMissions != null) {
                            for (MissionDto mission : legacyMissions) {
                                missions.put(mission.getMissionId(), mission);
                            }
                            LOG.debug("Migrated {} legacy pirate_missions to player_missions", legacyMissions.size());
                        }
                    } catch (JsonSyntaxException | IllegalStateException e) {
                        LOG.error("Failed to migrate legacy pirate_missions: {}", e.getMessage(), e);
                    }
                }
                // Load other state entries
                for (Map.Entry<String, JsonElement> entry : stateJson.entrySet()) {
                    if (!entry.getKey().equals(MISSIONS) && !entry.getKey().equals("pirate_missions")) {
                        state.put(entry.getKey(), GSON.fromJson(entry.getValue(), Object.class));
                    }
                }
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

            // Load detected signals
            if (json.has("detectedSignals")) {
                JsonArray signalsArray = json.getAsJsonArray("detectedSignals");
                for (JsonElement elem : signalsArray) {
                    detectedSignals.add(elem.getAsString());
                }
                LOG.debug("Deserialized {} detected signals", detectedSignals.size());
            }

            // Load other fields
            if (json.has("bountyCollectedThisSession")) {
                bountyCollectedThisSession = json.get("bountyCollectedThisSession").getAsLong();
            }
            if (json.has("aiVoice") && !json.get("aiVoice").isJsonNull()) {
                aiVoice = Voices.valueOf(json.get("aiVoice").getAsString());
            }
            if (json.has("aiPersonality") && !json.get("aiPersonality").isJsonNull()) {
                aiPersonality = AIPersonality.valueOf(json.get("aiPersonality").getAsString());
            }
            if (json.has("aiCadence") && !json.get("aiCadence").isJsonNull()) {
                aiCadence = AICadence.valueOf(json.get("aiCadence").getAsString());
            }
            if (json.has("bodySignal") && !json.get("bodySignal").isJsonNull()) {
                bodySignal = GSON.fromJson(json.get("bodySignal"), BaseEvent.class);
            }
            if (json.has("chatHistory")) {
                chatHistory = json.getAsJsonArray("chatHistory");
            }
            LOG.debug("Loaded system session from: {}", file.getPath());
        } catch (IOException | JsonSyntaxException e) {
            LOG.error("Failed to load system session from {}: {}", file.getPath(), e.getMessage(), e);
        }
    }

    public Object get(String key) {
        return state.get(key);
    }

    public void remove(String key) {
        state.remove(key);
        saveSession();
    }

    public void put(String key, Object data) {
        state.put(key, data);
        saveSession();
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

    public void addBounty(long totalReward) {
        bountyCollectedThisSession = bountyCollectedThisSession + totalReward;
        saveSession();
    }

    public long getBountyCollectedThisSession() {
        return bountyCollectedThisSession;
    }

    public void clearFssSignals() {
        detectedSignals.clear();
        saveSession();
    }

    public void setAIVoice(Voices voice) {
        this.aiVoice = voice;
        saveSession();
    }

    public Voices getAIVoice() {
        return this.aiVoice == null ? Voices.JAMES : this.aiVoice;
    }

    public void setAIPersonality(AIPersonality personality) {
        this.aiPersonality = personality;
        saveSession();
    }

    public AIPersonality getAIPersonality() {
        return this.aiPersonality == null ? AIPersonality.ROGUE : this.aiPersonality;
    }

    public void setAICadence(AICadence cadence) {
        this.aiCadence = cadence;
        saveSession();
    }

    public AICadence getAICadence() {
        return this.aiCadence == null ? AICadence.IMPERIAL : this.aiCadence;
    }

    public void addBodySignal(BaseEvent event) {
        this.bodySignal = event;
        saveSession();
    }

    public BaseEvent getBodySignal() {
        return this.bodySignal;
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

    public JsonArray getChatHistory() {
        return chatHistory;
    }

    public void appendToChatHistory(JsonObject userMessage, JsonObject assistantMessage) {
        chatHistory.add(userMessage);
        chatHistory.add(assistantMessage);
        saveSession();
    }

    public boolean isPrivacyModeOn() {
        return isPrivacyModeOn;
    }

    public void setPrivacyMode(boolean privacyModeOn) {
        isPrivacyModeOn = privacyModeOn;
        saveSession();
    }

    public void clearChatHistory() {
        chatHistory = new JsonArray();
        saveSession();
    }

    @Subscribe
    public void clearOnShutDown(ClearSessionCacheEvent event) {
        state.clear();
        detectedSignals.clear();
        missions.clear();
        routeMap.clear();
        bountyCollectedThisSession = 0;
        aiVoice = null;
        aiPersonality = null;
        aiCadence = null;
        bodySignal = null;
        chatHistory = new JsonArray();
        deleteSessionFile();
    }

    private void deleteSessionFile() {
        String root = System.getProperty("user.dir");
        File file = new File(root, SESSION_FILE);
        if (file.exists()) {
            try {
                Files.delete(Paths.get(file.getPath()));
                LOG.debug("Deleted system session file: {}", file.getPath());
            } catch (IOException e) {
                LOG.error("Failed to delete system session file {}: {}", file.getPath(), e.getMessage(), e);
            }
        }
    }

    @Subscribe
    public void onBounty(BountyEvent event) {
        addPirateBounty(event);
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

    @Subscribe
    public void onLoadGame(LoadGameEvent event) {
        loadSavedStateFromDisk();
    }

    @Subscribe
    public void onLoadSession(LoadSessionEvent event) {
        loadSavedStateFromDisk();
    }
}
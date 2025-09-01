package elite.companion.session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.companion.comms.voice.Voices;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.gameapi.journal.events.BountyEvent;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.comms.ai.AICadence;
import elite.companion.comms.ai.AIPersonality;
import elite.companion.util.GsonFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * SystemSession
 * A singleton instance. Keeps track of the current state of the Ship AI. Stores ship sensor data.
 * Provides a consumable method to retrieve the data. Used for the Ship AI interactions. Such as reactions or storing information for internal use.
 * Consumed by Grok / Ship interactions. Not used by Voice Interactions.
 *
 */
public class SystemSession {
    public static final String CURRENT_SYSTEM = "current_system";
    public static final String SHIP_LOADOUT_JSON = "ship_loadout_json";
    public static final String SUITE_LOADOUT_JSON = "suite_loadout_json";
    public static final String FINAL_DESTINATION = "final_destination";
    public static final String CURRENT_STATUS = "current_status";
    public static final String FSD_TARGET = "fsd_target";
    public static final String SHIP_CARGO = "ship_cargo";
    public static final String PRIVACY_MODE = "privacy_mode";
    public static final String RADION_TRANSMISSION_ON_OFF = "radio_transmission_on_off";
    public static final String ANNOUNCE_BODY_SCANS = "announce_body_scans";
    public static final String CURRENT_SYSTEM_DATA = "current_system_data";
    public static final String PIRATE_MISSIONS = "pirate_missions";
    public static final String PIRATE_BOUNTIES = "pirate_bounties";
    public static final String REPUTATION = "reputation";
    public static final String CARRIER_LOCATION = "carrier_location";
    public static final String CURRENT_LOCATION = "current_location";
    public static final String TARGET_FACTION_NAME = "target_faction_name";
    public static final String PROFILE = "profile";
    public static final String PERSONALITY = "personality";

    private static final SystemSession INSTANCE = new SystemSession();
    public static final String MATERIALS = "materials";
    public static final String ENGINEER_PROGRESS = "engineer_progress";
    private static final String FRIENDS_STATUS = "friends_status";
    private final Map<String, Object> state = new HashMap<>();
    private final Set<String> detectedSignals = new LinkedHashSet<>();
    private final Map<Long, BaseEvent> missions = new LinkedHashMap();
    private final Map<String, NavRouteDto> routeMap = new LinkedHashMap<>(); // Star system name to NavRouteDto
    private long bountyCollectedThisSession = 0;
    private Voices aiVoice;
    private AIPersonality aiPersonality;
    private AICadence aiCadence;
    private BaseEvent bodySignal;
    private JsonArray chatHistory = new JsonArray();


    private SystemSession() {
        state.put(FRIENDS_STATUS, new HashMap<String, String>());
        state.put(PIRATE_MISSIONS, new ArrayList<MissionDto>());
    }

    public static SystemSession getInstance() {
        return INSTANCE;
    }

    public Object get(String key) {
        return state.get(key);
    }

    public void remove(String key) {
        state.remove(key);
    }

    public void put(String key, Object data) {
        state.put(key, data);
    }

    public void addSignal(BaseEvent event) {
        detectedSignals.add(event.toJson());
    }

    public String getSignals() {
        Object[] array = detectedSignals.stream().toArray();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object o : array) {
            sb.append(o).append(", ");
        }
        sb.append("]");

        return array.length == 0 ? "no data" : sb.toString();
    }


    public void setNavRoute(Map<String, NavRouteDto> routeMap) {
        this.routeMap.clear();
        this.routeMap.putAll(routeMap);
    }

    public void removeNavPoint(String systemName) {
        routeMap.remove(systemName);
    }

    public Map<String, NavRouteDto> getRoute() {
        return routeMap;
    }

    public void clearRoute() {
        routeMap.clear();
    }

    public void addBounty(long totalReward) {
        bountyCollectedThisSession = bountyCollectedThisSession + totalReward;
    }

    public long getBountyCollectedThisSession() {
        return bountyCollectedThisSession;
    }

    public void clearFssSignals() {
        detectedSignals.clear();
    }

    public void setAIVoice(Voices voice) {
        this.aiVoice = voice;
    }

    public Voices getAIVoice() {
        return this.aiVoice == null ? Voices.JAMES : this.aiVoice;
    }

    public void setAIPersonality(AIPersonality personality) {
        this.aiPersonality = personality;
    }

    public AIPersonality getAIPersonality() {
        return this.aiPersonality == null ? AIPersonality.FRIENDLY : this.aiPersonality;
    }

    public void setAICadence(AICadence cadence) {
        this.aiCadence = cadence;
    }

    public AICadence getAICadence() {
        return this.aiCadence == null ? AICadence.IMPERIAL : this.aiCadence;
    }

    public void addBodySignal(BaseEvent event) {
        this.bodySignal = event;
    }

    public BaseEvent getBodySignal() {
        return this.bodySignal;
    }

    public void addMission(MissionDto mission) {
        missions.put(mission.getMissionId(), mission);
    }

    public Map<Long, BaseEvent> getMissions() {
        return missions;
    }

    public void removeMission(Long missionId) {
        missions.remove(missionId);
    }

    public BaseEvent getMission(Long missionId) {
        return missions.get(missionId);
    }

    public void clearMissions() {
        missions.clear();
    }

    public void addPirateMission(MissionDto mission) {
        List<MissionDto> missions = (List<MissionDto>) state.computeIfAbsent(PIRATE_MISSIONS, k -> new ArrayList<MissionDto>());
        missions.add(mission);
    }

    public void addPirateBounty(BountyEvent bounty) {
        List<BountyEvent> bounties = (List<BountyEvent>) state.computeIfAbsent(PIRATE_BOUNTIES, k -> new ArrayList<BountyEvent>());
        bounties.add(bounty);
    }

    public void removePirateMission(long missionID) {
        List<MissionDto> missions = (List<MissionDto>) state.get(PIRATE_MISSIONS);
        if (missions != null) {
            missions.removeIf(mission -> mission.getMissionId() == missionID);
        }
    }

    public void removeExpiredMissions() {
        List<MissionDto> missions = (List<MissionDto>) state.get(PIRATE_MISSIONS);
        if (missions != null) {
            missions.removeIf(mission -> {
                String expiry = GsonFactory.getGson().fromJson(mission.toJson(), JsonObject.class).get("Expiry").getAsString();
                return ZonedDateTime.parse(expiry).isBefore(ZonedDateTime.now(ZoneId.of("Z")));
            });
        }
    }

    public String getPirateMissionsJson() {
        List<MissionDto> missions = (List<MissionDto>) state.get(PIRATE_MISSIONS);
        if (missions == null || missions.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (MissionDto mission : missions) {
            sb.append(mission.toJson()).append(",");
        }
        sb.append("]");
        return sb.toString().replace(",]", "]");
    }

    public String getPirateBountiesJson() {
        List<BountyEvent> bounties = (List<BountyEvent>) state.get(PIRATE_BOUNTIES);
        if (bounties == null || bounties.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (BountyEvent bounty : bounties) {
            sb.append(bounty.toJson()).append(",");
        }
        sb.append("]");
        return sb.toString().replace(",]", "]");
    }

    // Add methods
    public JsonArray getChatHistory() {
        return chatHistory;
    }

    public void appendToChatHistory(JsonObject userMessage, JsonObject assistantMessage) {
        chatHistory.add(userMessage);
        chatHistory.add(assistantMessage);
    }

    public void clearChatHistory() {
        chatHistory = new JsonArray();
    }

    public void clearOnShutDown() {
        state.clear();
        detectedSignals.clear();
        missions.clear();
        routeMap.clear();
        bountyCollectedThisSession = 0;
    }
}
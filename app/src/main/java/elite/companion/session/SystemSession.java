package elite.companion.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.companion.comms.ai.AICadence;
import elite.companion.comms.ai.AIPersonality;
import elite.companion.comms.voice.Voices;
import elite.companion.gameapi.journal.events.LoadGameEvent;
import elite.companion.util.EventBusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SystemSession {
    private static final Logger LOG = LoggerFactory.getLogger(SystemSession.class);

    public static final String RADION_TRANSMISSION_ON_OFF = "radio_transmission_on_off";
    private static final String SESSION_FILE = "session/system_session.json";

    private static final SystemSession INSTANCE = new SystemSession();
    private final Map<String, Object> state = new HashMap<>();

    private Voices aiVoice;
    private AIPersonality aiPersonality;
    private AICadence aiCadence;
    private JsonArray chatHistory = new JsonArray();
    private boolean isPrivacyModeOn = false;
    private final SessionPersistence persistence = new SessionPersistence(SESSION_FILE);

    private SystemSession() {
        persistence.registerField("aiVoice", this::getAIVoice, this::setAIVoice, Voices.class);
        persistence.registerField("aiPersonality", this::getAIPersonality, this::setAIPersonality, AIPersonality.class);
        persistence.registerField("aiCadence", this::getAICadence, this::setAICadence, AICadence.class);
        persistence.registerField("chatHistory", this::getChatHistory, this::setChatHistory, JsonArray.class);
        persistence.registerField("isPrivacyModeOn", this::isPrivacyModeOn, this::setPrivacyMode, Boolean.class);
        EventBusManager.register(this);
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveSession));
    }

    public static SystemSession getInstance() {
        return INSTANCE;
    }

    public void saveSession() {
        persistence.saveSession(state);
    }

    private void loadSavedStateFromDisk() {
        persistence.loadSession(json -> persistence.loadFields(json, state));
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

    public JsonArray getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(JsonArray chatHistory) {
        this.chatHistory = chatHistory;
        saveSession();
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
        this.isPrivacyModeOn = privacyModeOn;
        saveSession();
    }

    public void clearChatHistory() {
        chatHistory = new JsonArray();
        saveSession();
    }

    @Subscribe
    public void clearOnShutDown(ClearSessionCacheEvent event) {
        state.clear();
        aiVoice = null;
        aiPersonality = null;
        aiCadence = null;
        chatHistory = new JsonArray();
        persistence.deleteSessionFile();
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
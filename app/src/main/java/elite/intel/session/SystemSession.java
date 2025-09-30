package elite.intel.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.ai.ConfigManager;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.LoadGameEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * The SystemSession class manages the state and configuration of the system session,
 * allowing persistence, loading, and manipulation of various properties such as AI voice,
 * personality, cadence, streaming settings, and chat history. This class maintains a singleton
 * instance to ensure a consistent session state throughout the application lifecycle.
 * <p>
 * Key responsibilities include:
 * - Managing session data through persistence and disk operations.
 * - Handling AI-related settings such as voice, personality, and cadence.
 * - Maintaining and modifying the chat history.
 * - Supporting streaming mode and resetting session data on shutdown or specific events.
 */
public class SystemSession extends SessionPersistence implements java.io.Serializable {
    private static final SystemSession INSTANCE = new SystemSession();
    private static final String SESSION_FILE = "system_session.json";

    public static final String RADION_TRANSMISSION_ON_OFF = "radio_transmission_on_off";
    private final Map<String, Object> state = new HashMap<>();

    private AiVoices aiVoice;
    private AIPersonality aiPersonality;
    private AICadence aiCadence;
    private JsonArray chatHistory = new JsonArray();
    private boolean isStreamingModeOn = false;

    public final static String RMS_THRESHOLD_HIGH = "RMS_THRESHOLD_HIGH";
    public final static String RMS_THRESHOLD_LOW = "RMS_THRESHOLD_LOW";


    private SystemSession() {
        ensureFileAndDirectoryExist(SESSION_FILE);
        registerField("aiVoice", this::getAIVoice, this::setAIVoice, AiVoices.class);
        registerField("aiPersonality", this::getAIPersonality, this::setAIPersonality, AIPersonality.class);
        registerField("aiCadence", this::getAICadence, this::setAICadence, AICadence.class);
        registerField("chatHistory", this::getChatHistory, this::setChatHistory, JsonArray.class);
        registerField("isPrivacyModeOn", this::isStreamingModeOn, this::setStreamingMode, Boolean.class);
        EventBusManager.register(this);
        loadSavedStateFromDisk();
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveSession));
    }

    public static SystemSession getInstance() {
        return INSTANCE;
    }

    public void saveSession() {
        saveSession(state);
    }

    private void loadSavedStateFromDisk() {
        loadSession(json -> {
            if (json != null) {
                loadFields(json, state);
            }
        });
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


    public void setAIVoice(AiVoices voice) {
        this.aiVoice = voice;
        saveSession();
    }

    public AiVoices getAIVoice() {
        return this.aiVoice == null ? AiVoices.EMMA : this.aiVoice;
    }

    public void setAIPersonality(AIPersonality personality) {
        this.aiPersonality = personality;
        saveSession();
    }

    public AIPersonality getAIPersonality() {
        return this.aiPersonality == null ? AIPersonality.CASUAL : this.aiPersonality;
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

    public boolean isStreamingModeOn() {
        return isStreamingModeOn;
    }

    public void setStreamingMode(boolean streamingModeOn) {
        this.isStreamingModeOn = streamingModeOn;
        saveSession();
    }

    public void clearChatHistory() {
        chatHistory = new JsonArray();
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

    public void clearSystemConfigValues() {
        state.remove(ConfigManager.STT_API_KEY);
        state.remove(ConfigManager.TTS_API_KEY);
        state.remove(ConfigManager.AI_API_KEY);
    }
}
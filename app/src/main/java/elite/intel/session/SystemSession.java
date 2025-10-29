package elite.intel.session;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.LoadGameEvent;

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
    private static volatile SystemSession instance;
    private static final String SESSION_FILE = "system_session.json";

    public static final String RADION_TRANSMISSION_ON_OFF = "radio_transmission_on_off";
    public static final String SESSION_DIR = "session/";
    public static final String LOGGING_ENABLED = "logging_enabled";
    public static final String BINDINGS_DIR = "bindings_dir";
    public static final String JOURNAL_DIR = "journal_dir";
    public static final String TTS_API_KEY = "tts_api_key";
    public static final String STT_API_KEY = "stt_api_key";
    public static final String AI_API_KEY = "ai_api_key";
    public static final String MISSION_STATEMENT = "mission_statement";
    public static final String ALTERNATIVE_NAME = "alternative_name";
    public static final String TITLE = "title";

    private final static String RMS_THRESHOLD_HIGH = "RMS_THRESHOLD_HIGH";
    private final static String RMS_THRESHOLD_LOW = "RMS_THRESHOLD_LOW";

    // Existing fields
    private AiVoices aiVoice;
    private AIPersonality aiPersonality;
    private AICadence aiCadence;
    private JsonArray chatHistory = new JsonArray();
    private boolean isStreamingModeOn = false;
    private Double rmsThresholdHigh = null;
    private Double rmsThresholdLow = null;

    // New fields for state replacement
    private boolean loggingEnabled = false;
    private String bindingsDir = "";
    private String journalDir = "";
    private String ttsApiKey = "";
    private String sttApiKey = "";
    private String aiApiKey = "";
    private String missionStatement = "";
    private String alternativeName = "";
    private String title = "";

    private SystemSession() {
        super(SESSION_DIR);
        ensureFileAndDirectoryExist(SESSION_FILE);
        registerField("aiVoice", this::getAIVoice, this::setAIVoice, AiVoices.class);
        registerField("aiPersonality", this::getAIPersonality, this::setAIPersonality, AIPersonality.class);
        registerField("aiCadence", this::getAICadence, this::setAICadence, AICadence.class);
        registerField("chatHistory", this::getChatHistory, this::setChatHistory, JsonArray.class);
        registerField("isPrivacyModeOn", this::isStreamingModeOn, this::setStreamingMode, Boolean.class);
        registerField(RMS_THRESHOLD_HIGH, this::getRmsThresholdHigh, this::setRmsThresholdHigh, Double.class);
        registerField(RMS_THRESHOLD_LOW, this::getRmsThresholdLow, this::setRmsThresholdLow, Double.class);

        // New field registrations
        registerField(LOGGING_ENABLED, this::isLoggingEnabled, this::setLoggingEnabled, Boolean.class);
        registerField(BINDINGS_DIR, this::getBindingsDir, this::setBindingsDir, String.class);
        registerField(JOURNAL_DIR, this::getJournalDir, this::setJournalDir, String.class);
        registerField(TTS_API_KEY, this::getTtsApiKey, this::setTtsApiKey, String.class);
        registerField(STT_API_KEY, this::getSttApiKey, this::setSttApiKey, String.class);
        registerField(AI_API_KEY, this::getAiApiKey, this::setAiApiKey, String.class);
        registerField(MISSION_STATEMENT, this::getMissionStatement, this::setMissionStatement, String.class);
        registerField(ALTERNATIVE_NAME, this::getAlternativeName, this::setAlternativeName, String.class);
        registerField(TITLE, this::getTitle, this::setTitle, String.class);

        EventBusManager.register(this);
        loadSavedStateFromDisk();
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    public static SystemSession getInstance() {
        if (instance == null) {
            synchronized (Status.class) {
                if (instance == null) {
                    instance = new SystemSession();
                }
            }
        }
        return instance;
    }

    private void loadSavedStateFromDisk() {
        loadSession(SystemSession.this::loadFields);
    }

    public AiVoices getAIVoice() {
        return this.aiVoice == null ? AiVoices.STEVE : this.aiVoice;
    }

    public void setAIVoice(AiVoices voice) {
        this.aiVoice = voice;
        save();
    }

    public void setAIPersonality(AIPersonality personality) {
        this.aiPersonality = personality;
        save();
    }

    public AIPersonality getAIPersonality() {
        return this.aiPersonality == null ? AIPersonality.CASUAL : this.aiPersonality;
    }

    public void setAICadence(AICadence cadence) {
        this.aiCadence = cadence;
        save();
    }

    public AICadence getAICadence() {
        return this.aiCadence == null ? AICadence.FEDERATION : this.aiCadence;
    }

    public JsonArray getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(JsonArray chatHistory) {
        this.chatHistory = chatHistory;
        save();
    }

    public void appendToChatHistory(JsonObject userMessage, JsonObject assistantMessage) {
        chatHistory.add(userMessage);
        chatHistory.add(assistantMessage);
        save();
    }

    public boolean isStreamingModeOn() {
        return isStreamingModeOn;
    }

    public void setStreamingMode(boolean streamingModeOn) {
        this.isStreamingModeOn = streamingModeOn;
        save();
    }

    public void clearChatHistory() {
        chatHistory = new JsonArray();
        save();
    }

    public Double getRmsThresholdHigh() {
        return rmsThresholdHigh;
    }

    public void setRmsThresholdHigh(Double rmsThresholdHigh) {
        this.rmsThresholdHigh = rmsThresholdHigh;
        save();
    }

    public Double getRmsThresholdLow() {
        return rmsThresholdLow;
    }

    public void setRmsThresholdLow(Double rmsThresholdLow) {
        this.rmsThresholdLow = rmsThresholdLow;
        save();
    }

    // New getters and setters
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
        save();
    }

    public String getBindingsDir() {
        return bindingsDir;
    }

    public void setBindingsDir(String bindingsDir) {
        this.bindingsDir = bindingsDir;
        save();
    }

    public String getJournalDir() {
        return journalDir;
    }

    public void setJournalDir(String journalDir) {
        this.journalDir = journalDir;
        save();
    }

    public String getTtsApiKey() {
        return ttsApiKey;
    }

    public void setTtsApiKey(String ttsApiKey) {
        this.ttsApiKey = ttsApiKey;
        save();
    }

    public String getSttApiKey() {
        return sttApiKey;
    }

    public void setSttApiKey(String sttApiKey) {
        this.sttApiKey = sttApiKey;
        save();
    }

    public String getAiApiKey() {
        return aiApiKey;
    }

    public void setAiApiKey(String aiApiKey) {
        this.aiApiKey = aiApiKey;
        save();
    }

    public String getMissionStatement() {
        return missionStatement;
    }

    public void setMissionStatement(String missionStatement) {
        this.missionStatement = missionStatement;
        save();
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
        save();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void clearSystemConfigValues() {
        setTtsApiKey(null);
        setSttApiKey(null);
        setAiApiKey(null);
        save();
    }
}
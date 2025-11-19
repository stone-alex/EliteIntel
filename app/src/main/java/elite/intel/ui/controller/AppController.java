package elite.intel.ui.controller;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.ConfigManager;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.AuxiliaryFilesMonitor;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.JournalParser;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.ui.event.StreamModelToggleEvent;
import elite.intel.ui.event.SystemShutDownEvent;
import elite.intel.ui.model.AppModelInterface;
import elite.intel.ui.view.AppViewInterface;
import elite.intel.util.SleepNoThrow;
import elite.intel.yt.StreamChatVocalizer;
import elite.intel.yt.YouTubeChatVocalizer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static elite.intel.ui.view.AppView.*;
import static elite.intel.util.StringUtls.capitalizeWords;

/**
 * The AppController class acts as the primary controller in the Model-View-Controller (MVC)
 * architectural pattern. It mediates communication between the model (AppModelInterface)
 * and the view (AppViewInterface) while handling business logic and user actions.
 * <p>
 * This class implements the AppControllerInterface and ActionListener to define
 * the core behavior of the application and handle UI actions respectively.
 * <p>
 * Responsibilities:
 * - Managing application configuration settings by handling system and user configurations.
 * - Starting, stopping, and monitoring auxiliary services, such as voice processing
 * and file monitoring systems.
 * - Managing streaming mode and system logging settings.
 * - Responding to user events via the event bus and UI components.
 * - Maintaining application state, such as whether services are running.
 * <p>
 * Key Features:
 * - Uses Google voices, personalities, and cadences to define system behavior.
 * - Implements an event-driven architecture using an event bus (EventBusManager).
 * - Starts and stops auxiliary processes dynamically based on user input.
 * - Includes streaming mode control for voice and text processing.
 * - Maintains logs for user, voice process, and system activities.
 * - Dynamic loading of configuration keys for runtime functionality.
 */
public class AppController implements AppControllerInterface, ActionListener {
    private final AppModelInterface model;
    private final AppViewInterface view;
    private final ConfigManager configManager = ConfigManager.getInstance();
    private boolean isServiceRunning = false;
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();
    AuxiliaryFilesMonitor fileMonitor = new AuxiliaryFilesMonitor();
    EarsInterface ears;
    MouthInterface mouth;
    AiCommandInterface brain;
    StreamChatVocalizer streamChatVocalizer;
    JournalParser journalParser = new JournalParser();

    public AppController(AppModelInterface model, AppViewInterface view) {
        this.model = model;
        this.view = view;
        this.view.addActionListener(this);
        // Initialize configs
        model.setSystemConfig(configManager.readSystemConfig());
        model.setUserConfig(configManager.readUserConfig());
        appendToLog("SYSTEM: Initialized configs");
        EventBusManager.register(this);
    }

    @Subscribe
    public void onUserInputEvent(UserInputEvent event) {
        appendToLog("PLAYER: " + event.getUserInput());
    }

    @Subscribe
    public void onVoiceProcessEvent(VocalisationRequestEvent event) {
        appendToLog("AI: " + event.getText());
    }

    @Subscribe
    public void onAppLogEvent(AppLogEvent event) {
        if (model.showSystemLog()) appendToLog("SYSTEM: " + event.getData());
    }

    @Override
    public void handleSaveSystemConfig() {
        Map<String, String> systemConfig = view.getSystemConfigInput();
        configManager.writeConfigFile(ConfigManager.SYSTEM_CONFIG_FILENAME, systemConfig, true);
        model.setSystemConfig(systemConfig);
        appendToLog("Saved system config");
    }

    private void appendToLog(String data) {
        String formattedTime = Instant.now()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSS"));
        model.appendLog(formattedTime+": "+data);
    }

    @Override
    public void handleSaveUserConfig() {
        Map<String, String> userConfig = view.getUserConfigInput();
        configManager.writeConfigFile(ConfigManager.USER_CONFIG_FILENAME, userConfig, true);
        model.setUserConfig(userConfig);
    }

    @Override
    public boolean startStopServices() {
        isServiceRunning = !isServiceRunning;

        if (isServiceRunning) {

            boolean haveKeys = true;
            String ttsApiKey = String.valueOf(configManager.getSystemKey(ConfigManager.TTS_API_KEY));
            if (ttsApiKey == null || ttsApiKey.trim().isEmpty() || ttsApiKey.equals("null")) {
                appendToLog("SYSTEM: TTS API key not found in system.conf. I have no mouth to speak with");
                isServiceRunning = false;
                haveKeys = false;
            }

            String sttApiKey = String.valueOf(configManager.getSystemKey(ConfigManager.STT_API_KEY));
            if (sttApiKey == null || sttApiKey.trim().isEmpty() || sttApiKey.equals("null")) {
                appendToLog("SYSTEM: STT API key not found in system.conf. I have no ears to hear with");
                isServiceRunning = false;
                haveKeys = false;
            }

            String aiApiKey = String.valueOf(configManager.getSystemKey(ConfigManager.AI_API_KEY));
            if (aiApiKey == null || aiApiKey.trim().isEmpty() || aiApiKey.equals("null")) {
                appendToLog("SYSTEM: AI API key not found in system.conf. I have no brain to process with");
                isServiceRunning = false;
                haveKeys = false;
            }

            if (!haveKeys) {
                return false;
            }


            boolean streamingModeOn = systemSession.isStreamingModeOn();
            systemSession.clearChatHistory();
            model.setStreamingModeOn(streamingModeOn);

            journalParser.start();
            fileMonitor.start();

            mouth = ApiFactory.getInstance().getMouthImpl();
            mouth.start();

            ears = ApiFactory.getInstance().getEarsImpl();
            ears.start();

            brain = ApiFactory.getInstance().getCommandEndpoint();
            brain.start();

            ConfigManager configManager = ConfigManager.getInstance();
            String mission_statement = configManager.getPlayerKey(ConfigManager.PLAYER_MISSION_STATEMENT);
            playerSession.setPlayerMissionStatement(mission_statement);

            appendToLog("Available voices: " + listVoices());
            appendToLog("Available personalities: " + listPersonalities());
            appendToLog("Available profiles: " + listCadences());
            isServiceRunning = true;
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Systems offline..."));
            // Stop services
            journalParser.stop();
            fileMonitor.stop();
            brain.stop();
            ears.stop();
            mouth.stop();
            streamChatVocalizer.stop();
            systemSession.clearChatHistory();
            systemSession.clearSystemConfigValues();
            playerSession.clearOnShutDown();
            isServiceRunning = false;
        }
        model.setServicesRunning(isServiceRunning);
        return isServiceRunning;
    }

    private String listVoices() {
        StringBuilder sb = new StringBuilder();
        AiVoices[] voices = AiVoices.values();
        sb.append("[");
        for (AiVoices voice : voices) {
            sb.append(capitalizeWords(voice.name())).append(", ");
        }
        sb.append("]");
        return sb.toString().replace(", ]", "]");
    }

    private String listPersonalities() {
        AIPersonality[] personalities = AIPersonality.values();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (AIPersonality personality : personalities) {
            sb.append(capitalizeWords(personality.name())).append(", ");
        }
        sb.append("]");
        return sb.toString().replace(", ]", "]");
    }

    private String listCadences() {
        AICadence[] cadences = AICadence.values();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (AICadence cadence : cadences) {
            sb.append(capitalizeWords(cadence.name())).append(", ");
        }
        sb.append("]");
        return sb.toString().replace(", ]", "]");
    }


    @Subscribe
    public void onStreamModeToggle(StreamModelToggleEvent event){
        JCheckBox temp = new JCheckBox();
        temp.setSelected(event.isStreaming());
        executeCommand(temp, ACTION_TOGGLE_STREAMING_MODE);
    }


    @Override
    public void toggleStreamingMode(boolean streamingModeOnOff) {
        appendToLog("Toggle streaming mode");
        systemSession.setStreamingMode(streamingModeOnOff);
        model.setStreamingModeOn(streamingModeOnOff);
        EventBusManager.publish(new AiVoxResponseEvent(streamingModeOnOff ? streamingModeIsOnMessage() : streamingModeIsOffMessage()));
    }

    @Override
    public void togglePrivacyMode(boolean isPrivacyModeEnabled) {
        if (isPrivacyModeEnabled) {
            ears.stop();
        } else {
            ears.start();
        }
        model.setPrivacyModeOn(isPrivacyModeEnabled);
    }

    private String streamingModeIsOffMessage() {
        return "I am listening";
    }

    private String streamingModeIsOnMessage() {
        return "Streaming mode is On. Voice to text will still be processing, but I will not react to you. Please prefix your command with word computer";
    }

    private void handleSelectJournalDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Journal Directory");
        chooser.setCurrentDirectory(ConfigManager.getInstance().getJournalPath().toFile().getAbsoluteFile());
        if (chooser.showOpenDialog(view.getUiComponent()) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            view.getUserConfigInput().put(ConfigManager.JOURNAL_DIR, selectedDir.getAbsolutePath());
            handleSaveUserConfig();
            appendToLog("Selected custom journal directory: " + selectedDir.getAbsolutePath());
        }
    }

    private void handleSelectBindingsDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Bindings Directory");
        chooser.setCurrentDirectory(ConfigManager.getInstance().getBindingsPath().toFile().getAbsoluteFile());
        if (chooser.showOpenDialog(view.getUiComponent()) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            view.getUserConfigInput().put(ConfigManager.BINDINGS_DIR, selectedDir.getAbsolutePath());
            handleSaveUserConfig();
            appendToLog("Selected custom bindings directory: " + selectedDir.getAbsolutePath());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        executeCommand(e.getSource(), command);
    }

    private void executeCommand(Object e, String command) {
        if (e instanceof JCheckBox) {
            if (ACTION_TOGGLE_SYSTEM_LOG.equals(command)) {
                boolean show = ((JCheckBox) e).isSelected();
                model.showSystemLog(show);
                appendToLog("Further System log will be " + (show ? "shown" : "filtered"));
            } else if (ACTION_TOGGLE_STREAMING_MODE.equals(command)) {
                boolean isSelected = ((JCheckBox) e).isSelected();
                toggleStreamingMode(isSelected);
            } else if (ACTION_TOGGLE_PRIVACY_MODE.equals(command)) {
                boolean isSelected = ((JCheckBox) e).isSelected();
                togglePrivacyMode(isSelected);
            }
        }

        if (e instanceof JButton) {
            if (ACTION_SAVE_SYSTEM_CONFIG.equals(command)) {
                handleSaveSystemConfig();
            } else if (ACTION_SAVE_USER_CONFIG.equals(command)) {
                handleSaveUserConfig();
            } else if (ACTION_TOGGLE_SERVICES.equals(command)) {
                setupControls(startStopServices());
            } else if (ACTION_SELECT_JOURNAL_DIR.equals(command)) {
                handleSelectJournalDir();
            } else if (ACTION_SELECT_BINDINGS_DIR.equals(command)) {
                handleSelectBindingsDir();
            } else if (ACTION_RECALIBRATE_AUTIO.equals(command)) {
                recalibrateAudio();
            }
        }
    }

    private void setupControls(boolean isServiceRunning) {
        view.setupControlls(isServiceRunning);
    }

    private void recalibrateAudio() {
        appendToLog("Recalibrating audio...");
        ears.stop();
        systemSession.setRmsThresholdHigh(null);
        systemSession.setRmsThresholdLow(null);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Recalibrating audio..."));
        SleepNoThrow.sleep(5000);
        ears.start();
    }


    @Subscribe
    public void onSystemShutdownEvent(SystemShutDownEvent event){
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("System shutting down..."));
        appendToLog("SYSTEM: Shutting down...");
        fileMonitor.stop();
        journalParser.stop();
        brain.stop();
        ears.stop();
        mouth.stop();
        model.setServicesRunning(false);
        systemSession.clearChatHistory();
        SleepNoThrow.sleep(5000);
        System.exit(0);
    }
}
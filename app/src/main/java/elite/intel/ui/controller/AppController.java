package elite.intel.ui.controller;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.ConfigManager;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.ai.brain.xai.GrokCommandEndPoint;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.ears.STTConnectionFailed;
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
import elite.intel.ui.event.SystemShutDownEvent;
import elite.intel.ui.model.AppModelInterface;
import elite.intel.ui.view.AppViewInterface;
import elite.intel.util.SleepNoThrow;

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
        systemSession.clearSystemConfigValues();
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

            brain = ApiFactory.getInstance().getCommandEndpoint();
            brain.start();

            mouth = ApiFactory.getInstance().getMouthImpl();
            mouth.start();

            ears = ApiFactory.getInstance().getEarsImpl();
            ears.start();

            ConfigManager configManager = ConfigManager.getInstance();
            String mission_statement = configManager.getPlayerKey(ConfigManager.PLAYER_MISSION_STATEMENT);
            playerSession.setPlayerMissionStatement(mission_statement);


            String llm = "";
            if (brain instanceof GrokCommandEndPoint) {
                llm = "x AI";
            } else {
                llm = "Open AI";
            }

            EventBusManager.publish(new AiVoxResponseEvent("Systems online... Using " + llm + "."));

            appendToLog(
                    systemSession.getAIVoice().getName() +
                            " is listening to you... AI is set to "
                            + capitalizeWords(systemSession.getAICadence().name()) + " "
                            + capitalizeWords(systemSession.getAIPersonality().name())
            );
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



    @Override
    public void toggleStreamingMode(boolean streamingModeOnOff) {
        appendToLog("Toggle streaming mode");
        systemSession.setStreamingMode(streamingModeOnOff);
        model.setStreamingModeOn(streamingModeOnOff);
        EventBusManager.publish(new AiVoxResponseEvent(streamingModeOnOff ? streamingModeIsOnMessage() : streamingModeIsOffMessage()));
        appendToLog(streamingModeOnOff ? streamingModeIsOnMessage() : streamingModeIsOffMessage());
    }

    @Override
    public void togglePrivacyMode(boolean isPrivacyModeEnabled) {
        if (isPrivacyModeEnabled) {
            EventBusManager.publish(new AiVoxResponseEvent("one way comms, voice input disabled."));
            ears.stop();
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Voice input enabled."));
            ears.start();
        }
        model.setPrivacyModeOn(isPrivacyModeEnabled);
    }

    private String streamingModeIsOffMessage() {
        return "Streaming mode is Off. " + systemSession.getAIVoice().getName() + " is listening to you.";
    }

    private String streamingModeIsOnMessage() {
        return "Streaming mode is On. (voice to text will still be processing, but I will not hear you. Prefix your command with word computer or " + systemSession.getAIVoice().getName() + ") ";
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

        if (e.getSource() instanceof JCheckBox) {
            String command = e.getActionCommand();
            if (ACTION_TOGGLE_SYSTEM_LOG.equals(command)) {
                boolean show = ((JCheckBox) e.getSource()).isSelected();
                model.showSystemLog(show);
                appendToLog("Further System log will be " + (show ? "shown" : "filtered"));
            } else if (ACTION_TOGGLE_STREAMING_MODE.equals(command)) {
                boolean isSelected = ((JCheckBox) e.getSource()).isSelected();
                toggleStreamingMode(isSelected);
            } else if (ACTION_TOGGLE_PRIVACY_MODE.equals(command)) {
                boolean isSelected = ((JCheckBox) e.getSource()).isSelected();
                togglePrivacyMode(isSelected);
            }
        }

        if (e.getSource() instanceof JButton) {
            String command = e.getActionCommand();
            if (ACTION_SAVE_SYSTEM_CONFIG.equals(command)) {
                handleSaveSystemConfig();
            } else if (ACTION_SAVE_USER_CONFIG.equals(command)) {
                handleSaveUserConfig();
            } else if (ACTION_TOGGLE_SERVICES.equals(command)) {
                ((JButton) e.getSource()).setText(startStopServices() ? "Stop Service" : "Start Service");
            } else if (ACTION_SELECT_JOURNAL_DIR.equals(command)) {
                handleSelectJournalDir();
            } else if (ACTION_SELECT_BINDINGS_DIR.equals(command)) {
                handleSelectBindingsDir();
            }
        }
    }


    @Subscribe
    public void onSystemShutdownEvent(SystemShutDownEvent event){
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("System shutting down..."));
        appendToLog("SYSTEM: Shutting down...");
        SleepNoThrow.sleep(5000);
        fileMonitor.stop();
        journalParser.stop();
        brain.stop();
        ears.stop();
        mouth.stop();
        systemSession.clearChatHistory();
        model.setServicesRunning(false);
        appendToLog("Exiting...");
        SleepNoThrow.sleep(1000);
        System.exit(0);
    }


    @Subscribe
    public void onSttConnectionFailed(STTConnectionFailed event) {
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Warning! Speech to Text connection failed. Attempting re-connect in 10 seconds"));
        ears = ApiFactory.getInstance().getEarsImpl();
        ears.stop();
        SleepNoThrow.sleep(10000);
        ears.start();
    }
}
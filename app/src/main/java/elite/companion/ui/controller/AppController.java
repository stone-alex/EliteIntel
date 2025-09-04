package elite.companion.ui.controller;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.voice.SpeechRecognizer;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.AuxiliaryFilesMonitor;
import elite.companion.gameapi.JournalParser;
import elite.companion.gameapi.UserInputEvent;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.session.SystemSession;
import elite.companion.ui.event.AppLogEvent;
import elite.companion.ui.model.AppModelInterface;
import elite.companion.ui.view.AppViewInterface;
import elite.companion.util.ConfigManager;
import elite.companion.util.EventBusManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import static elite.companion.ui.view.AppView.*;

public class AppController implements AppControllerInterface, ActionListener {
    private final AppModelInterface model;
    private final AppViewInterface view;
    private final ConfigManager configManager = ConfigManager.getInstance();
    private boolean isServiceRunning = false;

    AuxiliaryFilesMonitor fileMonitor = new AuxiliaryFilesMonitor();
    SpeechRecognizer speechRecognizer = new SpeechRecognizer();
    VoiceGenerator voiceGenerator;
    JournalParser journalParser = new JournalParser();

    public AppController(AppModelInterface model, AppViewInterface view) {
        this.model = model;
        this.view = view;
        this.view.addActionListener(this);
        // Initialize configs
        model.setSystemConfig(configManager.readSystemConfig());
        model.setUserConfig(configManager.readUserConfig());
        model.appendLog("Initialized configs");
        EventBusManager.register(this);
    }

    @Subscribe
    public void onUserInputEvent(UserInputEvent event) {
        model.appendLog("PLAYER: " + event.getUserInput());
    }

    @Subscribe
    public void onVoiceProcessEvent(VoiceProcessEvent event) {
        model.appendLog("AI: " + event.getText());
    }

    @Subscribe
    public void onAppLogEvent(AppLogEvent event) {
        if (model.showSystemLog()) model.appendLog("SYSTEM: " + event.getData());
    }

    @Override
    public void handleSaveSystemConfig() {
        Map<String, String> systemConfig = view.getSystemConfigInput();
        configManager.writeConfigFile(ConfigManager.SYSTEM_CONFIG_FILENAME, systemConfig, true);
        model.setSystemConfig(systemConfig);
        model.appendLog("Saved configs");
    }

    @Override public void handleSaveUserConfig() {
        Map<String, String> userConfig = view.getUserConfigInput();
        configManager.writeConfigFile(ConfigManager.USER_CONFIG_FILENAME, userConfig, true);
        model.setUserConfig(userConfig);
    }

    @Override
    public boolean handleStartStop() {
        isServiceRunning = !isServiceRunning;
        if (isServiceRunning) {

            String googleKey = String.valueOf(configManager.getSystemKey(ConfigManager.GOOGLE_API_KEY));
            if (googleKey == null || googleKey.trim().isEmpty() || googleKey.equals("null")) {
                model.appendLog("SYSTEM: Google API key not found in system.conf");
                isServiceRunning = false;
                return false;
            }

            String grokKey = String.valueOf(configManager.getSystemKey(ConfigManager.GROK_API_KEY));
            if (grokKey == null || grokKey.trim().isEmpty() || grokKey.equals("null")) {
                model.appendLog("SYSTEM: Grok API key not found in system.conf");
                isServiceRunning = false;
                return false;
            }


            boolean privacyModeOn = systemSession.isPrivacyModeOn();

            model.setPrivacyModeOn(privacyModeOn);

            journalParser.start();
            voiceGenerator = VoiceGenerator.getInstance();
            speechRecognizer.start();
            fileMonitor.start();
            voiceGenerator.start();
            EventBusManager.publish(new VoiceProcessEvent("Systems online..."));
            isServiceRunning = true;
        } else {
            EventBusManager.publish(new VoiceProcessEvent("Systems offline..."));
            // Stop services
            journalParser.stop();
            voiceGenerator.stop();
            voiceGenerator = null;
            speechRecognizer.stop();
            fileMonitor.stop();
            model.appendLog("Systems offline...");
            isServiceRunning = false;
        }
        model.setServicesRunning(isServiceRunning);
        return isServiceRunning;
    }

    private final SystemSession systemSession = SystemSession.getInstance();

    @Override
    public void togglePrivacyMode(boolean privacyModeOn) {
        model.appendLog("Toggle privacy mode");
        systemSession.setPrivacyMode(privacyModeOn);
        model.setPrivacyModeOn(privacyModeOn);
        EventBusManager.publish(new VoiceProcessEvent(privacyModeOn ? privacyModeIsOnMessage() : privacyModeIsOffMessage()));
        model.appendLog(privacyModeOn ? privacyModeIsOnMessage() : privacyModeIsOffMessage());
    }

    private String privacyModeIsOffMessage() {
        return "Privacy mode is Off. " + systemSession.getAIVoice().getName() + " is listening to you.";
    }

    private String privacyModeIsOnMessage() {
        return "Privacy mode is On. (voice to text will still be processing, but " + systemSession.getAIVoice().getName() + " will not hear you. Prefix your command with word computer or " + systemSession.getAIVoice().getName() + ") ";
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() instanceof JCheckBox) {
            String command = e.getActionCommand();
            if (ACTION_TOGGLE_SYSTEM_LOG.equals(command)) {
                boolean show = ((JCheckBox) e.getSource()).isSelected();
                model.showSystemLog(show);
                model.appendLog("Further System log will be " + (show ? "shown" : "filtered"));
            } else if (ACTION_TOGGLE_PRIVACY_MODE.equals(command)) {
                boolean isSelected = ((JCheckBox) e.getSource()).isSelected();
                togglePrivacyMode(isSelected);
                //((JCheckBox) e.getSource()).setText(this.model.isPrivacyModeOn() ? "Privacy ON" : "Privacy OFF");
            }
        }

        if (e.getSource() instanceof JButton) {
            String command = e.getActionCommand();
            if (ACTION_SAVE_SYSTEM_CONFIG.equals(command)) {
                handleSaveSystemConfig();
            } else if (ACTION_SAVE_USER_CONFIG.equals(command)) {
                handleSaveUserConfig();
            } else if (ACTION_TOGGLE_SERVICES.equals(command)) {
                ((JButton) e.getSource()).setText(handleStartStop() ? "Stop Service" : "Start Service");
            }
        }
    }
}
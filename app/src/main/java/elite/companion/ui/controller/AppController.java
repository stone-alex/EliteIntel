package elite.companion.ui.controller;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.voice.SpeechRecognizer;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.AuxiliaryFilesMonitor;
import elite.companion.gameapi.UserInputEvent;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.ui.model.AppModelInterface;
import elite.companion.ui.view.AppView;
import elite.companion.ui.view.AppViewInterface;
import elite.companion.util.ConfigManager;
import elite.companion.util.EventBusManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class AppController implements AppControllerInterface, ActionListener {
    private final AppModelInterface model;
    private final AppViewInterface view;
    private final ConfigManager configManager = ConfigManager.getInstance();
    private boolean isServiceRunning = false;

    AuxiliaryFilesMonitor fileMonitor = new AuxiliaryFilesMonitor();
    SpeechRecognizer speechRecognizer = new SpeechRecognizer();
    VoiceGenerator voiceGenerator;

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
        model.appendLog("User: " + event.getUserInput());
    }

    @Subscribe
    public void onVoiceProcessEvent(VoiceProcessEvent event) {
        model.appendLog("AI: " + event.getText());
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
    public void handleStartStop() {
        isServiceRunning = !isServiceRunning;
        if (isServiceRunning) {
            voiceGenerator = VoiceGenerator.getInstance();
            speechRecognizer.start();
            fileMonitor.start();
            voiceGenerator.start();
            model.appendLog("Systems online...");

            EventBusManager.publish(new VoiceProcessEvent("Systems online..."));
        } else {
            EventBusManager.publish(new VoiceProcessEvent("Systems offline..."));
            try {
                Thread.sleep(5000);
                // Stop services
                voiceGenerator.stop(); voiceGenerator = null;
                speechRecognizer.stop();
                fileMonitor.stop();
                model.appendLog("Systems offline...");
            } catch (InterruptedException e) {
                //
            }
        }
    }

    @Override
    public void handlePushToTalk() {
        // Trigger voice recognition (TOS-compliant, user-initiated)
        //speechRecognizer.start();
        model.appendLog("Push-to-talk activated");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            String command = e.getActionCommand();
            if (AppView.ACTION_SAVE_SYSTEM_CONFIG.equals(command)) {
                handleSaveSystemConfig();
            } else if (AppView.ACTION_SAVE_USER_CONFIG.equals(command)) {
                handleSaveUserConfig();
            } else if (AppView.ACTION_TOGGLE_SERVICES.equals(command)) {
                handleStartStop();
                ((JButton) e.getSource()).setText(isServiceRunning ? "Stop Service" : "Start Service");
            } else if ("Push to Talk".equals(command)) {
                handlePushToTalk();
            }
        }
    }
}
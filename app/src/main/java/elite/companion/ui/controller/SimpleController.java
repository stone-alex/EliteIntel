package elite.companion.ui.controller;

import elite.companion.comms.voice.SpeechRecognizer;
import elite.companion.ui.model.IModel;
import elite.companion.ui.view.IView;
import elite.companion.util.ConfigManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class SimpleController implements IController, ActionListener {
    private final IModel model;
    private final IView view;
    private final ConfigManager configManager = ConfigManager.getInstance();
    private boolean isServiceRunning = false;

    public SimpleController(IModel model, IView view) {
        this.model = model;
        this.view = view;
        this.view.addActionListener(this);
        // Initialize configs
        model.setSystemConfig(configManager.readSystemConfig());
        model.setUserConfig(configManager.readUserConfig());
        model.appendLog("Initialized configs");
    }

    @Override
    public void handleSave() {
        Map<String, String> systemConfig = view.getSystemConfigInput();
        Map<String, String> userConfig = view.getUserConfigInput();
        configManager.writeConfigFile(ConfigManager.SYSTEM_CONFIG_FILENAME, systemConfig, true);
        configManager.writeConfigFile(ConfigManager.USER_CONFIG_FILENAME, userConfig, true);

        model.setSystemConfig(systemConfig);
        model.setUserConfig(userConfig);
        model.appendLog("Saved configs");
    }

    @Override
    public void handleStartStop() {
        isServiceRunning = !isServiceRunning;
        if (isServiceRunning) {
            // Start event bus, journal parser, etc.
            model.appendLog("Service started");
        } else {
            // Stop services
            model.appendLog("Service stopped");
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
            if ("Save".equals(command)) {
                handleSave();
            } else if ("Start Service".equals(command) || "Stop Service".equals(command)) {
                handleStartStop();
                ((JButton) e.getSource()).setText(isServiceRunning ? "Stop Service" : "Start Service");
            } else if ("Push to Talk".equals(command)) {
                handlePushToTalk();
            }
        }
    }
}
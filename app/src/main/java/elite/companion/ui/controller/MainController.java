package elite.companion.ui.controller;

import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.util.ConfigManager;
import elite.companion.util.EventBusManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private final ConfigManager configManager = ConfigManager.getInstance();
    private Stage stage; // Set by MainApp

    @FXML private TextField googleApiKeyField;
    @FXML private TextField grokApiKeyField;
    @FXML private Button saveButton;
    @FXML private Button testVoiceButton;
    @FXML private CheckBox minimizeCheckBox;
    @FXML private TextArea journalLog;

    public MainController() {
        EventBusManager.register(this); // Subscribe to event bus
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        googleApiKeyField.setText(configManager.readSystemConfig().get(ConfigManager.GOOGLE_API_KEY));
        grokApiKeyField.setText(configManager.readSystemConfig().get(ConfigManager.GROK_API_KEY));
        journalLog.setText("Companion App Ready\n");
    }

    @FXML
    private void saveApiKey() {
        String apiKey = googleApiKeyField.getText();
        String grokApiKey = grokApiKeyField.getText();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            configManager.readSystemConfig().put(ConfigManager.GOOGLE_API_KEY, apiKey);
            configManager.readSystemConfig().put(ConfigManager.GROK_API_KEY, grokApiKey);
            journalLog.appendText("Google API key saved.\n");
            log.info("Google API key updated in system.conf");
            EventBusManager.publish(new VoiceProcessEvent("API keys updated successfully"));
        } else {
            journalLog.appendText("Error: Invalid API key.\n");
            log.error("Invalid Google API key entered");
        }
    }

    @FXML
    private void testVoice() {
        EventBusManager.publish(new VoiceProcessEvent("Testing voice trigger: Grok is online"));
        journalLog.appendText("Voice trigger test sent.\n");
    }

    @FXML
    private void toggleMinimize() {
        if (minimizeCheckBox.isSelected()) {
            stage.setIconified(true); // Minimize to taskbar
            journalLog.appendText("Minimized for VR mode.\n");
            EventBusManager.publish(new VoiceProcessEvent("App minimized for VR"));
        } else {
            stage.setIconified(false);
            journalLog.appendText("Restored window.\n");
        }
    }

    @com.google.common.eventbus.Subscribe
    public void handleJournalEvent(BaseEvent event) {
        Platform.runLater(() -> {
            journalLog.appendText("Journal Event: " + event.getEventType() + " at " + event.getTimestamp() + "\n");
        });
    }

    @com.google.common.eventbus.Subscribe
    public void handleVoiceEvent(VoiceProcessEvent event) {
        Platform.runLater(() -> {
            journalLog.appendText("Voice Event: " + event.getText() + "\n");
        });
    }
}
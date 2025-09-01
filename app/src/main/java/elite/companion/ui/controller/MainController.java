package elite.companion.ui.controller;

import elite.companion.gameapi.SensorDataEvent;
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
    @FXML private TextField edsmApiKeyField;
    @FXML private Button saveButton;
    @FXML private Button testVoiceButton;
    @FXML private CheckBox minimizeCheckBox;
    @FXML private TextArea journalLog;
    @FXML private CheckBox lockGoogleKeyFieldCheckBox;
    @FXML private CheckBox lockGrokFieldCheckBox;
    @FXML private CheckBox lockEdsmFieldCheckBox;

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
        edsmApiKeyField.setText(configManager.readSystemConfig().get(ConfigManager.EDSM_KEY));
        journalLog.setText("Companion App Ready\n");

        googleApiKeyField.setDisable(true);
        lockGoogleKeyFieldCheckBox.setSelected(true);
        lockGoogleKeyFieldCheckBox.setText("Locked");

        grokApiKeyField.setDisable(true);
        lockGrokFieldCheckBox.setSelected(true);
        lockGrokFieldCheckBox.setText("Locked");

        edsmApiKeyField.setDisable(true);
        lockEdsmFieldCheckBox.setSelected(true);
        lockEdsmFieldCheckBox.setText("Locked");
    }

    @FXML
    private void saveApiKey() {
        String googleApiKey = googleApiKeyField.getText();
        String grokApiKey = grokApiKeyField.getText();
        String edsmApiKey = edsmApiKeyField.getText();

        if (googleApiKey != null && !googleApiKey.trim().isEmpty()) {
            configManager.readSystemConfig().put(ConfigManager.GOOGLE_API_KEY, googleApiKey);
            journalLog.appendText("Google API key saved.\n");
        }
        if(grokApiKey != null && !grokApiKey.trim().isEmpty()) {
            configManager.readSystemConfig().put(ConfigManager.GROK_API_KEY, grokApiKey);
            journalLog.appendText("Grok API key saved.\n");
        }
        if(edsmApiKey != null && !edsmApiKey.trim().isEmpty()){
            configManager.readSystemConfig().put(ConfigManager.EDSM_KEY, edsmApiKey);
            journalLog.appendText("EDSM API key saved.\n");
        }
        EventBusManager.publish(new VoiceProcessEvent("Configuration file saved."));
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


    @FXML
    private void onLockGoogleKeyMouseClicked() {
        if (lockGoogleKeyFieldCheckBox.isSelected()) {
            googleApiKeyField.setDisable(true);
            lockGoogleKeyFieldCheckBox.setText("Locked");
        } else {
            googleApiKeyField.setDisable(false);
            lockGoogleKeyFieldCheckBox.setText("Unlocked");
        }
    }

    @FXML
    private void lockGrokKeyMouseClicked() {
        if (lockGrokFieldCheckBox.isSelected()) {
            grokApiKeyField.setDisable(true);
            lockGrokFieldCheckBox.setText("Locked");
        } else {
            grokApiKeyField.setDisable(false);
            lockGrokFieldCheckBox.setText("Unlocked");
        }
    }

    @FXML
    private void onLockEdsmMouseClicked(){
        if(lockEdsmFieldCheckBox.isSelected()){
            edsmApiKeyField.setDisable(true);
            lockEdsmFieldCheckBox.setText("Locked");
        }else{
            edsmApiKeyField.setDisable(false);
            lockEdsmFieldCheckBox.setText("Unlocked");
        }
    }

    @com.google.common.eventbus.Subscribe
    public void handleJournalEvent(BaseEvent event) {
        Platform.runLater(() -> {
            journalLog.appendText("Journal Event: " + event.getEventType() + "\n");
        });
    }

    @com.google.common.eventbus.Subscribe
    public void handleVoiceEvent(VoiceProcessEvent event) {
        Platform.runLater(() -> {
            journalLog.appendText("Voice Event: " + event.getText() + "\n");
        });
    }

    @com.google.common.eventbus.Subscribe
    public void handleSystemEvent(SensorDataEvent event) {
        Platform.runLater(() -> {
            journalLog.appendText("System Event: " + event.getSensorData() + "\n");
        });
    }
}
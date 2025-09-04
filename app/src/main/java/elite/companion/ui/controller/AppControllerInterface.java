package elite.companion.ui.controller;

public interface AppControllerInterface {
    void handleSaveSystemConfig();
    void handleSaveUserConfig();
    boolean handleStartStop();
    void togglePrivacyMode(boolean isOn);
}
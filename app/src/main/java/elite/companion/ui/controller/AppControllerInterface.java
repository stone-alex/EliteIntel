package elite.companion.ui.controller;

public interface AppControllerInterface {
    void handleSaveSystemConfig();
    void handleSaveUserConfig();
    boolean startStopServices();
    void togglePrivacyMode(boolean isOn);
}
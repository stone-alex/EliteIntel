package elite.intel.ui.controller;

public interface AppControllerInterface {
    void handleSaveSystemConfig();
    void handleSaveUserConfig();
    boolean startStopServices();
    void toggleStreamingMode(boolean isOn);

    void togglePrivacyMode(boolean isPrivacyModeEnabled);
}
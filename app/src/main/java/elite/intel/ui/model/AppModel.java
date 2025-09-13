package elite.intel.ui.model;

import elite.intel.ai.ConfigManager;
import elite.intel.ui.view.AppView;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * The AppModel class implements the AppModelInterface and serves as the data model for the application.
 * It manages system and user-level configurations, logs, and application state.
 * This class provides support for property change listeners to notify observers of state changes.
 */
public class AppModel implements AppModelInterface {
    private String log = "";
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean streamingMode;
    private boolean privacyModeOn;
    private boolean showSystemLog;

    @Override
    public Map<String, String> getSystemConfig() {
        return ConfigManager.getInstance().readSystemConfig();
    }

    @Override
    public Map<String, String> getUserConfig() {
        return ConfigManager.getInstance().readUserConfig();
    }

    @Override
    public void setSystemConfig(Map<String, String> config) {
        Map<String, String> oldConfig = getSystemConfig();
        Map<String, String> systemConfig = new HashMap<>(config);
        pcs.firePropertyChange(AppView.PROPERTY_SYSTEM_CONFIG_UPDATED, oldConfig, systemConfig);
    }

    @Override
    public void setUserConfig(Map<String, String> config) {
        Map<String, String> oldConfig = getUserConfig();
        Map<String, String> userConfig = new HashMap<>(config);
        pcs.firePropertyChange(AppView.PROPERTY_USER_CONFIG_UPDATED, oldConfig, userConfig);
    }

    @Override
    public void appendLog(String message) {
        String oldLog = log;
        log += message + "\n";
        pcs.firePropertyChange(AppView.PROPERTY_LOG_UPDATED, oldLog, log);
    }

    @Override
    public String getHelpText() {
        //noinspection TextBlockMigration
        return "<html>\n" +
                "<body>\n" +
                "<h1>Elite Companion Help</h1>\n" +
                "<p>Welcome to the Elite Companion App, a quality-of-life tool for Elite Dangerous players. This guide covers the setup steps to get started, including installing Java, obtaining API keys, and configuring the app.</p>\n" +
                "\n" +
                "<h2>2. Obtaining Google Speech-to-Text (STT) and Text-to-Speech (TTS) Keys</h2>\n" +
                "<p>The app uses Google Cloud for voice interaction. You need API keys for STT and TTS services:</p>\n" +
                "<ul>\n" +
                "    <li><b>Create a Google Cloud Account</b>: Sign up at https://cloud.google.com or log in with an existing Google account.</li>\n" +
                "    <li><b>Set Up a Project</b>: In the Google Cloud Console, create a new project (e.g., \"EliteCompanion\").</li>\n" +
                "    <li><b>Enable APIs</b>: Go to \"APIs & Services\" > \"Library,\" search for \"Cloud Speech-to-Text API\" and \"Cloud Text-to-Speech API,\" and enable both.</li>\n" +
                "    <li><b>Create API Key</b>:\n" +
                "        <ul>\n" +
                "            <li>Navigate to \"APIs & Services\" > \"Credentials.\"</li>\n" +
                "            <li>Click \"Create Credentials\" > \"API Key.\"</li>\n" +
                "            <li>Copy the generated key (e.g., \"AIza...\").</li>\n" +
                "            <li>Restrict the key to STT and TTS APIs for security (optional but recommended).</li>\n" +
                "        </ul>\n" +
                "    </li>\n" +
                "    <li><b>Add to App</b>: In the app’s \"System\" tab, enter the API key in the \"Google API Key\" field and save the configuration.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>3. Obtaining an xAI API Key</h2>\n" +
                "<p>The app uses xAI’s Grok API for intelligent responses. Follow these steps to get your key:</p>\n" +
                "<ul>\n" +
                "    <li><b>Visit xAI</b>: Go to https://x.ai/api (x.ai/api) and sign in or create an account.</li>\n" +
                "    <li><b>Request API Access</b>: Follow the instructions to request an API key for Grok. You may need to provide details about your use case.</li>\n" +
                "    <li><b>Copy the Key</b>: Once approved, you’ll receive an API key (e.g., \"xai_...\").</li>\n" +
                "    <li><b>Add to App</b>: In the app’s \"System\" tab, enter the key in the \"Grok API Key\" field and save the configuration.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>4. Configuring the App</h2>\n" +
                "<p>After setting up Java and API keys, configure the app for optimal use:</p>\n" +
                "<ul>\n" +
                "    <li><b>System Tab</b>: Enter and save your Google API Key, Grok API Key, and EDSM API Key (if used). Check the \"Locked\" boxes to prevent accidental changes.</li>\n" +
                "    <li><b>Player Tab</b>: Set your alternative name, title, and mission statement to personalize the app.</li>\n" +
                "    <li><b>Start Services</b>: Click \"Start Services\" to begin processing game journal files and enabling voice interaction.</li>\n" +
                "    <li><b>Journal Files</b>: Ensure Elite Dangerous journal files are in <code>(YOUR USER HOME DIRECTORY)/Saved Games/Frontier Developments/Elite Dangerous</code> (Windows) or equivalent on your platform.</li>\n" +
                "    <li><b>Voice Interaction</b>: Use voice commands (via Google STT) to interact with the app. Responses will be spoken via Google TTS.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>5. Troubleshooting</h2>\n" +
                "<ul>\n" +
                "    <li><b>Java Issues</b>: If the app doesn’t start, verify Java 17 is installed and on your PATH using <code>java -version</code>.</li>\n" +
                "    <li><b>API Key Errors</b>: Ensure keys are correctly entered and not restricted from the required APIs. Check logs in the \"System\" tab for details.</li>\n" +
                "    <li><b>Journal Files</b>: If events aren’t processed, verify Elite Dangerous is writing journal files to the expected directory.</li>\n" +
                "    <li><b>Voice Issues</b>: Ensure your microphone and speakers are working, and the Google API key is valid.</li>\n" +
                "    <li><b>Logs</b>: Check <code>elite-intel.log</code> in the app’s directory for detailed error messages.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<p>For further assistance, visit the Elite Dangerous community forums or contact the app developers.</p>\n" +
                "</body>\n" +
                "</html>";
    }


    @Override public void showSystemLog(boolean show) {
        this.showSystemLog = show;
    }

    @Override public boolean showSystemLog() {
        return this.showSystemLog;
    }


    @Override public void setStreamingModeOn(boolean on) {
        this.streamingMode = on;
        pcs.firePropertyChange(AppView.PROPERTY_STREAMING_MODE, !on, on);
    }

    @Override
    public void setPrivacyModeOn(boolean on) {
        this.privacyModeOn = on;
        pcs.firePropertyChange(AppView.PROPERTY_PRIVACY_MODE, !on, on);
    }

    @Override public void setServicesRunning(boolean isServiceRunning) {
        pcs.firePropertyChange(AppView.PROPERTY_SERVICES_TOGGLE, !isServiceRunning, isServiceRunning);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
}
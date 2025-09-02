package elite.companion.ui.model;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public class SimpleModel implements IModel {
    private Map<String, String> systemConfig = new HashMap<>();
    private Map<String, String> userConfig = new HashMap<>();
    private String log = "";
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public Map<String, String> getSystemConfig() {
        return new HashMap<>(systemConfig); // Defensive copy
    }

    @Override
    public Map<String, String> getUserConfig() {
        return new HashMap<>(userConfig); // Defensive copy
    }

    @Override
    public void setSystemConfig(Map<String, String> config) {
        Map<String, String> oldConfig = getSystemConfig();
        systemConfig = new HashMap<>(config);
        pcs.firePropertyChange("systemConfig", oldConfig, systemConfig);
    }

    @Override
    public void setUserConfig(Map<String, String> config) {
        Map<String, String> oldConfig = getUserConfig();
        userConfig = new HashMap<>(config);
        pcs.firePropertyChange("userConfig", oldConfig, userConfig);
    }

    @Override
    public void appendLog(String message) {
        String oldLog = log;
        log += message + "\n";
        pcs.firePropertyChange("log", oldLog, log);
    }

    @Override
    public String getHelpText() {
        return "<html><b>Elite Companion Help</b><br>" +
                "<ul>" +
                "<li><b>Get API Keys</b>: Register at xAI for grok_key, Google Cloud for google_api_key, EDSM for edsm_key.</li>" +
                "<li><b>Usage</b>: Enter configs, lock fields to prevent changes, save to apply.</li>" +
                "<li><b>Issues</b>: Ensure Elite Dangerous journal files are accessible.</li>" +
                "</ul></html>";
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}
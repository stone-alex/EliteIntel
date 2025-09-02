package elite.companion.ui.model;

import java.beans.PropertyChangeListener;
import java.util.Map;

public interface AppModelInterface {
    Map<String, String> getSystemConfig();
    Map<String, String> getUserConfig();
    void setSystemConfig(Map<String, String> config);
    void setUserConfig(Map<String, String> config);
    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
    void appendLog(String message);
    String getHelpText();
}
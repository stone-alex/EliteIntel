package elite.companion.ui.view;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Map;

public interface AppViewInterface {
    void displaySystemConfig(Map<String, String> config);
    void displayUserConfig(Map<String, String> config);
    void displayHelp(String helpText);
    Map<String, String> getSystemConfigInput();
    Map<String, String> getUserConfigInput();
    void addActionListener(ActionListener listener);
    JFrame getUiComponent();
}
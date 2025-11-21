package elite.intel.ui.view;

import javax.swing.*;
import java.awt.event.ActionListener;

public interface AppViewInterface {
    void initData();
    JFrame getUiComponent();
    void setServicesRunning(boolean running);
}
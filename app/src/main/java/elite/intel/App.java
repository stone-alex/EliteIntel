package elite.intel;

import elite.intel.ai.ConfigManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SubscriberRegistration;
import elite.intel.session.LoadSessionEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.controller.AppController;
import elite.intel.ui.model.AppModel;
import elite.intel.ui.model.AppModelInterface;
import elite.intel.ui.view.AppView;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;


public class App {

    public static void main(String[] args) {

        // Toggle logging via system.conf
        boolean isLoggingEnabled = "TRUE".equalsIgnoreCase(ConfigManager.getInstance().getSystemKey(ConfigManager.DEBUG_SWITCH));
        Configurator.setRootLevel(isLoggingEnabled ? Level.ALL : Level.OFF);

        // init
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.getSystemKey(ConfigManager.AI_API_KEY);
        configManager.getSystemKey(ConfigManager.YT_API_KEY);
        configManager.getSystemKey(ConfigManager.TTS_API_KEY);
        configManager.getSystemKey(ConfigManager.STT_API_KEY);

        SubscriberRegistration.registerSubscribers();
        //noinspection ResultOfMethodCallIgnored
        PlayerSession.getInstance(); //Initialize player session
        //noinspection ResultOfMethodCallIgnored
        SystemSession.getInstance(); //Initialize system session
        EventBusManager.publish(new LoadSessionEvent());
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel( new FlatLightLaf() );
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppModelInterface model = new AppModel();
            AppView view = new AppView();
            new AppController(model, view);
            model.addPropertyChangeListener(view);
            view.displaySystemConfig(model.getSystemConfig());
            view.displayUserConfig(model.getUserConfig());
            view.getUiComponent().setVisible(true);
        });
    }
}
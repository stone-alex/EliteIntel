package elite.intel;

import com.formdev.flatlaf.FlatLightLaf;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SubscriberRegistration;
import elite.intel.session.LoadSessionEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.controller.AppController;
import elite.intel.ui.event.ToggleServicesEvent;
import elite.intel.ui.view.AppView;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;


public class App {

    public static void main(String[] args) {

        //DB init first.
        Database.init(); // init db


        SystemSession systemSession = SystemSession.getInstance();
        boolean isLoggingEnabled = systemSession.isLoggingEnabled();
        Configurator.setRootLevel(isLoggingEnabled ? Level.ALL : Level.OFF);
        SubscriberRegistration.registerSubscribers();
        //noinspection ResultOfMethodCallIgnored
        PlayerSession.getInstance();
        //noinspection ResultOfMethodCallIgnored
        systemSession.setStreamingMode(false);
        EventBusManager.publish(new LoadSessionEvent());
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppView view = new AppView();
            new AppController(view);
            view.getUiComponent().setVisible(true);
        });
    }
}
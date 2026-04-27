package elite.intel;

import com.formdev.flatlaf.FlatLightLaf;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SubscriberRegistration;
import elite.intel.session.LoadSessionEvent;
import elite.intel.ui.controller.AppController;
import elite.intel.ui.view.AppView;
import elite.intel.util.Cypher;
import elite.intel.ws.LlmActionBroadcaster;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;


public class App {

    public static void main(String[] args) {

        // init kry and db first!
        Cypher.initializeKey();
        Database.init();

        LlmActionBroadcaster.getInstance().start();

        // change the debug log level when we have version 1.0
        Configurator.setRootLevel(Level.ALL);

        // Event subscribers
        SubscriberRegistration.registerSubscribers();

        // spin up the session
        EventBusManager.publish(new LoadSessionEvent());

        // init UI
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppView view = new AppView();
            new AppController();
            view.getUiComponent().setVisible(true);
        });
    }
}
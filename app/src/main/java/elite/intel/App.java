package elite.intel;

//import elite.intel.ui.controller.MainController;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class App {
    private static final Logger LOGGER = LogManager.getLogger(App.class);
    /**
     * The entry point of the application.
     * This method initializes critical components, sets up the user interface,
     * and starts the main event loop of the application.
     * <p>
     * The following tasks are performed in this method:
     * - Registers event subscribers to the event bus system.
     * - Initializes player and system sessions.
     * - Publishes a session load event.
     * - Sets the look-and-feel of the user interface.
     * - Initializes the MVC (Model-View-Controller) structure.
     * - Displays system configuration, user configuration, and help text in the main view.
     * <p>
     * This method runs the main user interface logic on the Event Dispatch Thread (EDT)
     * to ensure all UI interactions conform to Swing's threading model.
     *
     * @param args Command-line arguments passed to the application. These arguments are not used.
     */
    public static void main(String[] args) {

        // Suppress console output
//        System.setOut(new PrintStream(new OutputStream() {
//            @Override public void write(int b) {}
//        }));
//        System.setErr(new PrintStream(new OutputStream() {
//            @Override public void write(int b) {}
//        }));

        // Toggle logging via system.conf
        boolean isLoggingEnabled = "TRUE".equalsIgnoreCase(ConfigManager.getInstance().getSystemKey(ConfigManager.DEBUG_SWITCH));
        Configurator.setRootLevel(isLoggingEnabled ? Level.ALL : Level.OFF);




        SubscriberRegistration.registerSubscribers();
        //noinspection ResultOfMethodCallIgnored
        PlayerSession.getInstance(); //Initialize player session
        //noinspection ResultOfMethodCallIgnored
        SystemSession.getInstance(); //Initialize system session
        EventBusManager.publish(new LoadSessionEvent());

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppModelInterface model = new AppModel();
            AppView view = new AppView();
            new AppController(model, view);
            model.addPropertyChangeListener(view);
            view.displaySystemConfig(model.getSystemConfig());
            view.displayUserConfig(model.getUserConfig());
            view.displayHelp(model.getHelpText());
            view.getUiComponent().setVisible(true);
        });
    }
}
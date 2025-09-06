package elite.companion;

//import elite.companion.ui.controller.MainController;

import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SubscriberRegistration;
import elite.companion.session.LoadSessionEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.ui.controller.AppController;
import elite.companion.ui.model.AppModel;
import elite.companion.ui.model.AppModelInterface;
import elite.companion.ui.view.AppView;

import javax.swing.*;

public class App {

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
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
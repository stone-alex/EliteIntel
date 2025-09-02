package elite.companion;

//import elite.companion.ui.controller.MainController;

import elite.companion.ui.controller.AppController;
import elite.companion.ui.model.AppModel;
import elite.companion.ui.model.AppModelInterface;
import elite.companion.ui.view.AppView;
import elite.companion.util.SubscriberRegistration;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        SubscriberRegistration.registerSubscribers();

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
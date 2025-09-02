package elite.companion.ui;

//import elite.companion.ui.controller.MainController;
import elite.companion.ui.controller.SimpleController;
import elite.companion.ui.model.IModel;
import elite.companion.ui.model.SimpleModel;
import elite.companion.ui.view.ConfigMainView;

import javax.swing.*;

public class SwingMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            IModel model = new SimpleModel();
            ConfigMainView view = new ConfigMainView();
            new SimpleController(model, view);
            model.addPropertyChangeListener(view);
            view.displaySystemConfig(model.getSystemConfig());
            view.displayUserConfig(model.getUserConfig());
            view.displayHelp(model.getHelpText());
            view.getUiComponent().setVisible(true);
        });
    }
}
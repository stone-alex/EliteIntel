package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.CenterPanel;
import elite.intel.session.ui.UINavigator;

public class DeploySrvHandler extends CommandOperator implements CommandHandler {

    public DeploySrvHandler(GameController handler) {
        super(handler.getMonitor(), handler.getExecutor());
    }

    private final UINavigator navigator = new UINavigator(this);
    private final Status status = Status.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {

        if (status.isInMainShip()) {
            navigator.openAndNavigate(StatusFlags.GuiFocus.ROLE_PANEL, CenterPanel.SRV);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_ACTIVATE.getGameBinding(), 0);
        }
    }
}

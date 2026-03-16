package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.CenterPanel;
import elite.intel.session.ui.UINavigator;

public class OpenFighterPanelHandler extends CommandOperator implements CommandHandler {


    private final UINavigator navigator = new UINavigator(this);
    private final Status status = Status.getInstance();

    public OpenFighterPanelHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip() || status.isInSrv()) {
            operateKeyboard(Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0);
            navigator.openAndNavigate(StatusFlags.GuiFocus.ROLE_PANEL, CenterPanel.FIGHTER);
        }
    }
}

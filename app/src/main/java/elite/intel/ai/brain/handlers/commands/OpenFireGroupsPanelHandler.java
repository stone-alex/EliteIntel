package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.RightPanel;
import elite.intel.session.ui.UINavigator;

public class OpenFireGroupsPanelHandler extends CommandOperator implements CommandHandler {

    public OpenFireGroupsPanelHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }


    private final UINavigator navigator = new UINavigator(this);
    private final Status status = Status.getInstance();


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip() || status.isInSrv() || status.isInFighter()) {
            operateKeyboard(Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0);
            navigator.openAndNavigate(StatusFlags.GuiFocus.INTERNAL_PANEL, RightPanel.FIRE_GROUPS);
        }
    }
}

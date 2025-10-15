package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_FOCUS_RADAR_PANEL;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_FOCUS_RADAR_PANEL_BUGGY;

public class DisplayRadarPanelHandler extends CustomCommandOperator implements CommandHandler {

    public DisplayRadarPanelHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            operateKeyboard(BINDING_FOCUS_RADAR_PANEL.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(BINDING_FOCUS_RADAR_PANEL_BUGGY.getGameBinding(), 0);
        }
    }
}

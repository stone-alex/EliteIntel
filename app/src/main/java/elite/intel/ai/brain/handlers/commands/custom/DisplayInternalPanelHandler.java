package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.FOCUS_INTERNAL_PANEL;
import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.FOCUS_INTERNAL_PANEL_BUGGY;

public class DisplayInternalPanelHandler extends CustomCommandOperator implements CommandHandler {


    public DisplayInternalPanelHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            operateKeyboard(FOCUS_INTERNAL_PANEL.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(FOCUS_INTERNAL_PANEL_BUGGY.getGameBinding(), 0);
        }

    }
}

package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_FOCUS_COMMS_PANEL;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_FOCUS_COMMS_PANEL_BUGGY;

public class DisplayCommsPanelHandler extends CustomCommandOperator implements CommandHandler {


    public DisplayCommsPanelHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInSrv()) {
            String command = BINDING_FOCUS_COMMS_PANEL_BUGGY.getGameBinding();
            operateKeyboard(command, 0);

        }

        if (status.isInMainShip()) {
            String command = BINDING_FOCUS_COMMS_PANEL.getGameBinding();
            operateKeyboard(command, 0);
        }
    }
}

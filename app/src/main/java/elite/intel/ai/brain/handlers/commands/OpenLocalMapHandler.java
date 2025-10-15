package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_LOCAL_MAP;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_LOCAL_MAP_BUGGY;

public class OpenLocalMapHandler extends CustomCommandOperator implements CommandHandler {


    public OpenLocalMapHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if (status.isInMainShip()) {
            operateKeyboard(BINDING_LOCAL_MAP.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(BINDING_LOCAL_MAP_BUGGY.getGameBinding(), 0);
        }
    }
}

package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.LOCAL_MAP;
import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.LOCAL_MAP_BUGGY;

public class OpenLocalMapHandler extends CustomCommandOperator implements CommandHandler {


    public OpenLocalMapHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        Status status = Status.getInstance();
        if (status.isInMainShip()) {
            operateKeyboard(LOCAL_MAP.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(LOCAL_MAP_BUGGY.getGameBinding(), 0);
        }
    }
}

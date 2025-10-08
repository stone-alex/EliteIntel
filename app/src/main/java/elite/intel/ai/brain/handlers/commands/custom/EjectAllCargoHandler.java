package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.EJECT_ALL_CARGO;
import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.EJECT_ALL_CARGO_BUGGY;

public class EjectAllCargoHandler extends CustomCommandOperator implements CommandHandler {

    public EjectAllCargoHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            operateKeyboard(EJECT_ALL_CARGO.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(EJECT_ALL_CARGO_BUGGY.getGameBinding(), 0);
        }

    }
}

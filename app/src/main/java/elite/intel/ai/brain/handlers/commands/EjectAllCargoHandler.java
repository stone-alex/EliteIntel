package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EJECT_ALL_CARGO;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EJECT_ALL_CARGO_BUGGY;

public class EjectAllCargoHandler extends CustomCommandOperator implements CommandHandler {

    public EjectAllCargoHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            operateKeyboard(BINDING_EJECT_ALL_CARGO.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(BINDING_EJECT_ALL_CARGO_BUGGY.getGameBinding(), 0);
        }

    }
}

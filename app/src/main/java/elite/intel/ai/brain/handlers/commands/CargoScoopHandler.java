package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_TOGGLE_CARGO_SCOOP;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_TOGGLE_CARGO_SCOOP_BUGGY;

public class CargoScoopHandler extends CommandOperator implements CommandHandler {


    public CargoScoopHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            operateKeyboard(BINDING_TOGGLE_CARGO_SCOOP.getGameBinding(), 0);
        }

        if (status.isInSrv()) {
            operateKeyboard(BINDING_TOGGLE_CARGO_SCOOP_BUGGY.getGameBinding(), 0);
        }
    }
}

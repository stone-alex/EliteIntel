package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_TOGGLE_CARGO_SCOOP;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_TOGGLE_CARGO_SCOOP_BUGGY;

public class OpenCargoScoopHandler extends CustomCommandOperator implements CommandHandler {


    public OpenCargoScoopHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            if (status.isCargoScoopDeployed()) {
                EventBusManager.publish(new AiVoxResponseEvent("Cargo scoop already deployed."));
            } else {
                operateKeyboard(BINDING_TOGGLE_CARGO_SCOOP.getGameBinding(), 0);
            }
        }

        if (status.isInSrv()) {
            if (status.isCargoScoopDeployed()) {
                EventBusManager.publish(new AiVoxResponseEvent("Cargo scoop already deployed."));
            } else {
                operateKeyboard(BINDING_TOGGLE_CARGO_SCOOP_BUGGY.getGameBinding(), 0);
            }
        }
    }
}

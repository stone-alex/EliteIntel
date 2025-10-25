package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_HARDPOINTS_TOGGLE;

public class RetractHardpointsHandler extends CommandOperator implements CommandHandler {

    public RetractHardpointsHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if(status.isInMainShip()) {
            if (status.isHardpointsDeployed()) {
                operateKeyboard(BINDING_HARDPOINTS_TOGGLE.getGameBinding(), 0);
            } else {
                EventBusManager.publish(new AiVoxResponseEvent("Hardpoints already retracted."));
            }
        }
    }
}

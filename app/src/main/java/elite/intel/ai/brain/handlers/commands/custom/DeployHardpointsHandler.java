package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.ControllerBindings.GameCommand.BINDING_HARDPOINTS_TOGGLE;

public class DeployHardpointsHandler extends CustomCommandOperator implements CommandHandler {

    public DeployHardpointsHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isHardpointsDeployed()) {
            EventBusManager.publish(new AiVoxResponseEvent("Hardpoints already deployed"));

        } else {
            operateKeyboard(BINDING_HARDPOINTS_TOGGLE.getGameBinding(), 0);
        }
    }
}

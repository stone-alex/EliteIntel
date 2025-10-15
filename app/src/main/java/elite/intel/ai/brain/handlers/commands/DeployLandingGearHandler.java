package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_LANDING_GEAR_TOGGLE;

public class DeployLandingGearHandler extends CustomCommandOperator implements CommandHandler {

    public DeployLandingGearHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isLandingGearDown()) {
            EventBusManager.publish(new AiVoxResponseEvent("Landing gear already deployed."));
        } else {
            operateKeyboard(BINDING_LANDING_GEAR_TOGGLE.getGameBinding(), 0);
        }
    }
}

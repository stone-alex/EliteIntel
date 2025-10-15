package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_LANDING_GEAR_TOGGLE;

public class RetractLandingGearHandler extends CustomCommandOperator implements CommandHandler {

    public RetractLandingGearHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isDocked() || status.isLanded()) {
            EventBusManager.publish(new AiVoxResponseEvent("Can not do that while landed or docked."));
            return;
        }

        if (status.isLandingGearDown()) {
            operateKeyboard(BINDING_LANDING_GEAR_TOGGLE.getGameBinding(), 0);
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Landing gear already retracted."));
        }
    }
}

package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.HARDPOINTS_TOGGLE;
import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.JUMP_TO_HYPERSPACE;

public class JumpToHyperspaceHandler extends CustomCommandOperator implements CommandHandler {

    public JumpToHyperspaceHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

        Status status = Status.getInstance();

        if (status.isFsdCharging()) return;

        if (status.isFsdMassLocked()) {
            EventBusManager.publish(new AiVoxResponseEvent("We are mass locked, FTL is not available."));
        } else if (status.isFsdCooldown()) {
            EventBusManager.publish(new AiVoxResponseEvent("FSD is on cooldown."));
        } else if (status.isInMainShip()) {
            if (status.isHardpointsDeployed()) {
                operateKeyboard(HARDPOINTS_TOGGLE.getGameBinding(), 0);
                SleepNoThrow.sleep(2000);
            }
            operateKeyboard(JUMP_TO_HYPERSPACE.getGameBinding(), 0);
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Get in to your ship, so we can blast out of here."));
        }
    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class EnterFtlHandler extends CommandOperator implements CommandHandler {

    public EnterFtlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isFsdCharging()) return;

        if (status.isFsdMassLocked()) {
            EventBusManager.publish(new AiVoxResponseEvent("We are mass locked, FTL is not available."));
        } else if (status.isFsdCooldown()) {
            EventBusManager.publish(new AiVoxResponseEvent("FSD is on cooldown."));
        } else if (status.isInMainShip()) {
            if (status.isHardpointsDeployed()) {
                operateKeyboard(BINDING_HARDPOINTS_TOGGLE.getGameBinding(), 0);
                SleepNoThrow.sleep(2000);
            }
            if(status.isCargoScoopDeployed()) {
                operateKeyboard(BINDING_TOGGLE_CARGO_SCOOP.getGameBinding(), 0);
                SleepNoThrow.sleep(2000);
            }
            if (status.isInSupercruise()) {
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
                operateKeyboard(BINDING_JUMP_TO_HYPERSPACE.getGameBinding(), 0);
                SleepNoThrow.sleep(12_000);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
            } else {
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
                operateKeyboard(BINDING_ENTER_SUPERCRUISE.getGameBinding(), 0);
                SleepNoThrow.sleep(12_000);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
            }
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Get in to your ship, so we can blast out of here."));
        }
    }
}

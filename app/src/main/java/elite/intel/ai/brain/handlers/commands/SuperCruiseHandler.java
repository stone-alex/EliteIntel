package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class SuperCruiseHandler extends CommandOperator implements CommandHandler {

    public SuperCruiseHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    private final UINavigator navigator = new UINavigator(this);
    private final Status status = Status.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        navigator.closeOpenPanel();

        if (status.isFsdCharging()) return;

        if (status.isFsdMassLocked()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("We are mass locked, FTL is not available."));
        } else if (status.isFsdCooldown()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("FSD is on cooldown."));
        } else if (status.isFighterOut()) {
            operateKeyboard(BINDING_REQUEST_REQUEST_DOCK.getGameBinding(), 0);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Fighter is still out. Can not comply."));
        } else if (status.isInMainShip()) {
            if (status.isInSupercruise()) {
                navigator.closeOpenPanel();
                operateKeyboard(BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
                operateKeyboard(BINDING_JUMP_TO_HYPERSPACE.getGameBinding(), 0);
                SleepNoThrow.sleep(12_000);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
            } else {
                PreFtlChecks.preJumpCheck(status, this, "Preparing for Supercruise.");
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
                operateKeyboard(BINDING_ENTER_SUPERCRUISE.getGameBinding(), 0);
                SleepNoThrow.sleep(1_000);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
                SleepNoThrow.sleep(1_000);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
                SleepNoThrow.sleep(1_000);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
                SleepNoThrow.sleep(1_000);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
                SleepNoThrow.sleep(1_000);
                operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
            }
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Get in to your ship, so we can blast out of here."));
        }
    }

}

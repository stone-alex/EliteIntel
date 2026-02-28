package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class JumpToHyperspaceHandler extends CommandOperator implements CommandHandler {

    public JumpToHyperspaceHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        Status status = Status.getInstance();

        if (status.isFsdCharging()) return;

        if (status.isFsdMassLocked()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("We are mass locked, FTL is not available."));
        } else if (status.isFsdCooldown()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("FSD is on cooldown."));
        } else if (status.isInMainShip()) {
            PreFtlChecks.preJumpCheck(status, this);
            operateKeyboard(BINDING_SET_SPEED100.getGameBinding(), 0);
            operateKeyboard(BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0);
            operateKeyboard(BINDING_JUMP_TO_HYPERSPACE.getGameBinding(), 0);
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Get in to your ship, so we can blast out of here."));
        }
    }
}

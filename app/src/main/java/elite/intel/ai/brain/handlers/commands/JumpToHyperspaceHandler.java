package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.data.FsdTarget;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_JUMP_TO_HYPERSPACE;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM;

public class JumpToHyperspaceHandler implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();


    @Override public void handle(String action, JsonObject params, String responseText) {
        GameControllerBus.publish(new GameInputEvent(BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0));
        SleepNoThrow.sleep(800);
        FsdTarget fsdTarget = playerSession.getFsdTarget();
        if (fsdTarget != null) {
            String starName = fsdTarget.getName() == null ? "unknown" : fsdTarget.getName();
            String fuelStatus = fsdTarget.getFuelStarStatus() == null ? "unknown" : fsdTarget.getFuelStarStatus();
            String instructions = """
                    Announce hyperspace jump destination name and re-fuel availability at destination.
                        Example 1: Jumping to Sol, fuel available at destination.
                        Example 2: Jumping to Atari, WARNING! No fuel available at destination.
                    """;
            EventBusManager.publish(
                    new SensorDataEvent(
                            "Jumping to: " + starName + ", " + fuelStatus,
                            instructions
                    )
            );
        }

        Status status = Status.getInstance();

        if (status.isFsdCharging()) return;

        if (status.isFsdMassLocked()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("We are mass locked, FTL is not available."));
        } else if (status.isFsdCooldown()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("FSD is on cooldown."));
        } else if (status.isInMainShip()) {
            PreFtlChecks.preJumpCheck(status, "Preparing for FTL.");
            GameControllerBus.publish(new GameInputEvent(BINDING_JUMP_TO_HYPERSPACE.getGameBinding(), 0));
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Get in to your ship, so we can blast out of here."));
        }
    }
}

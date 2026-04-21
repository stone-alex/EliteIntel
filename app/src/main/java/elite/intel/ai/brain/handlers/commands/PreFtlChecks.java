package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.GlobalSettingsManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class PreFtlChecks {


    public static void preJumpCheck(Status status, String message) {
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(message));
        GlobalSettingsManager settingsManager = GlobalSettingsManager.getInstance();
        if (status.isHardpointsDeployed() && !status.isInSupercruise()) {
            if (settingsManager.getAutoHardpointsRetractForFtl()) {
                GameControllerBus.publish(new GameInputEvent(BINDING_HARDPOINTS_TOGGLE.getGameBinding(), 0));
            }
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Retracting hardpoints."));
            SleepNoThrow.sleep(2000);
        }
        if (status.isLandingGearDown() && !status.isInSupercruise()) {
            if (settingsManager.getAutoLandingGearUpForFtl()) {
                GameControllerBus.publish(new GameInputEvent(BINDING_LANDING_GEAR_TOGGLE.getGameBinding(), 0));
            }
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Retracting landing gear."));
            SleepNoThrow.sleep(2000);
        }
        if (status.isCargoScoopDeployed() && !status.isInSupercruise()) {
            if (settingsManager.getAutoCargoScoopRetractForFtl()) {
                GameControllerBus.publish(new GameInputEvent(BINDING_TOGGLE_CARGO_SCOOP.getGameBinding(), 0));
            }
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Closing cargo bay."));
            SleepNoThrow.sleep(2000);
        }

        if (settingsManager.getAutoSpeedUpForFtl()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
        }
    }
}

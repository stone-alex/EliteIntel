package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class PreFtlChecks {


    public static void preJumpCheck(Status status, CommandOperator commandOperator) {
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Preparing for FTL."));
        if (status.isHardpointsDeployed()) {
            commandOperator.operateKeyboard(BINDING_HARDPOINTS_TOGGLE.getGameBinding(), 0);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Retracting hardpoints."));
            SleepNoThrow.sleep(2000);
        }
        if (status.isLandingGearDown()) {
            commandOperator.operateKeyboard(BINDING_LANDING_GEAR_TOGGLE.getGameBinding(), 0);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Retracting landing gear."));
            SleepNoThrow.sleep(2000);
        }
        if (status.isCargoScoopDeployed()) {
            commandOperator.operateKeyboard(BINDING_TOGGLE_CARGO_SCOOP.getGameBinding(), 0);
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Closing cargo bay."));
            SleepNoThrow.sleep(2000);
        }
        if (status.isNightVision()) {
            commandOperator.operateKeyboard(BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), 0);
            SleepNoThrow.sleep(250);
        }
        if (status.isLightsOn()) {
            commandOperator.operateKeyboard(BINDING_SHIP_LIGHTS_TOGGLE.getGameBinding(), 0);
            SleepNoThrow.sleep(250);
        }
    }
}

package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.GlobalSettingsManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.hands.Bindings.GameCommand.*;

public class PreFtlChecks {


    public static void preJumpCheck(Status status, String message) {
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(message));
        GlobalSettingsManager settingsManager = GlobalSettingsManager.getInstance();
        if (status.isHardpointsDeployed() && !status.isInSupercruise() && settingsManager.getAutoHardpointsRetractForFtl()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_HARDPOINTS_TOGGLE.getGameBinding(), 0));
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Retracting hardpoints."));
            SleepNoThrow.sleep(2000);
        }
        if (status.isLandingGearDown() && !status.isInSupercruise() && settingsManager.getAutoLandingGearUpForFtl()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_LANDING_GEAR_TOGGLE.getGameBinding(), 0));
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Retracting landing gear."));
            SleepNoThrow.sleep(2000);
        }
        if (status.isCargoScoopDeployed() && !status.isInSupercruise() && settingsManager.getAutoCargoScoopRetractForFtl()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_TOGGLE_CARGO_SCOOP.getGameBinding(), 0));
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Closing cargo bay."));
            SleepNoThrow.sleep(2000);
        }

        if (status.isLightsOn() && settingsManager.getAutoLightsForFtl()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_SHIP_LIGHTS_TOGGLE.getGameBinding(), 0));
        }

        if (status.isNightVision() && settingsManager.getAutoNightVisionOff()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), 0));
        }

        if (!status.isInSupercruise() && status.isFighterOut() && settingsManager.getAutoFighterOutFighterDocking()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_REQUEST_REQUEST_DOCK.getGameBinding(), 0));
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Fighter is still out. Can not comply."));
        }

        if (settingsManager.getAutoSpeedUpForFtl()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
        }
    }
}

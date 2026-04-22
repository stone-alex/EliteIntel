package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.GlobalSettingsManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

public class SuperCruiseHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();
    private final GlobalSettingsManager settingsManager = GlobalSettingsManager.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        navigator.closeOpenPanel();

        if (status.isFsdCharging()) return;

        if (status.isFsdMassLocked()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("We are mass locked, FTL is not available."));
        } else if (status.isFsdCooldown()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("FSD is on cooldown."));
        } else if (status.isFighterOut()) {
            GameControllerBus.publish(new GameInputEvent(BINDING_REQUEST_REQUEST_DOCK.getGameBinding(), 0));
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Fighter is still out. Can not comply."));
        } else if (status.isInMainShip()) {
            if (status.isInSupercruise()) {
                navigator.closeOpenPanel();
                GameControllerBus.publish(new GameInputEvent(BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0));
                if (settingsManager.getAutoSpeedUpForFtl()) {
                    GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
                }
                GameControllerBus.publish(new GameInputEvent(BINDING_JUMP_TO_HYPERSPACE.getGameBinding(), 0));
                if (settingsManager.getAutoSpeedUpForFtl()) {
                    SleepNoThrow.sleep(12_000);
                    GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
                }
            } else {
                PreFtlChecks.preJumpCheck(status, "Preparing for Supercruise.");
                if (settingsManager.getAutoSpeedUpForFtl()) {
                    GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
                }
                GameControllerBus.publish(new GameInputEvent(BINDING_ENTER_SUPERCRUISE.getGameBinding(), 0));
                if (settingsManager.getAutoSpeedUpForFtl()) {
                    SleepNoThrow.sleep(1_000);
                    GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
                    SleepNoThrow.sleep(1_000);
                    GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
                    SleepNoThrow.sleep(1_000);
                    GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
                    SleepNoThrow.sleep(1_000);
                    GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
                    SleepNoThrow.sleep(1_000);
                    GameControllerBus.publish(new GameInputEvent(BINDING_SET_SPEED100.getGameBinding(), 0));
                }
            }
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Get in to your ship, so we can blast out of here."));
        }
    }

}

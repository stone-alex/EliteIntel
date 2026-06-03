package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.GlobalSettingsManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.ui.UINavigator;

import static elite.intel.ai.hands.Bindings.GameCommand.*;

public class SuperCruiseHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();
    private final GlobalSettingsManager settingsManager = GlobalSettingsManager.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        UiNavCommon.close();

        if (status.isFsdCharging()) return;

        if (status.isFsdMassLocked()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("We are mass locked, FTL is not available."));
        } else if (status.isFsdCooldown()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("FSD is on cooldown."));
        } else if (status.isFighterOut()) {
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_REQUEST_REQUEST_DOCK.getGameBinding())));
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Fighter is still out. Can not comply."));
        } else if (status.isInMainShip()) {
            if (status.isInSupercruise()) {
                navigator.closeOpenPanel();
                if (settingsManager.getAutoSpeedUpForFtl()) {
                    GameControllerBus.publish(GameInputSequenceEvent.of(
                            GameInputStep.bindingTap(BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding()),
                            GameInputStep.bindingTap(BINDING_SET_SPEED100.getGameBinding()),
                            GameInputStep.bindingTap(BINDING_JUMP_TO_HYPERSPACE.getGameBinding()),
                            GameInputStep.delay(12_000),
                            GameInputStep.bindingTap(BINDING_SET_SPEED100.getGameBinding())
                    ));
                } else {
                    GameControllerBus.publish(GameInputSequenceEvent.of(
                            GameInputStep.bindingTap(BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding()),
                            GameInputStep.bindingTap(BINDING_JUMP_TO_HYPERSPACE.getGameBinding())
                    ));
                }
            } else {
                PreFtlChecks.preJumpCheck(status, "Preparing for Supercruise.");
                if (settingsManager.getAutoSpeedUpForFtl()) {
                    GameControllerBus.publish(GameInputSequenceEvent.of(
                            GameInputStep.bindingTap(BINDING_SET_SPEED100.getGameBinding()),
                            GameInputStep.bindingTap(BINDING_ENTER_SUPERCRUISE.getGameBinding()),
                            GameInputStep.delay(1_000),
                            GameInputStep.bindingTap(BINDING_SET_SPEED100.getGameBinding()),
                            GameInputStep.delay(1_000),
                            GameInputStep.bindingTap(BINDING_SET_SPEED100.getGameBinding()),
                            GameInputStep.delay(1_000),
                            GameInputStep.bindingTap(BINDING_SET_SPEED100.getGameBinding()),
                            GameInputStep.delay(1_000),
                            GameInputStep.bindingTap(BINDING_SET_SPEED100.getGameBinding()),
                            GameInputStep.delay(1_000),
                            GameInputStep.bindingTap(BINDING_SET_SPEED100.getGameBinding())
                    ));
                } else {
                    GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_ENTER_SUPERCRUISE.getGameBinding())));
                }
            }
        } else {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Get in to your ship, so we can blast out of here."));
        }
    }

}

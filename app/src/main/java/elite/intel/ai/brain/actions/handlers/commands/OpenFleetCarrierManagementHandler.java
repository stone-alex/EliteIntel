package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.*;

public class OpenFleetCarrierManagementHandler implements CommandHandler {


    private final Status status = Status.getInstance();

    /// not a sure fire. assumes default UI selection. will fail often.
    @Override
    public void handle(String action, JsonObject params, String responseText) {
        UiNavCommon.close();
        if (status.isOnFoot()) {
            GameControllerBus.publish(GameInputSequenceEvent.of(
                    GameInputStep.bindingHold(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500),
                    GameInputStep.bindingTap(BINDING_UI_RIGHT.getGameBinding()),
                    GameInputStep.bindingTap(BINDING_UI_DOWN.getGameBinding()),
                    GameInputStep.bindingTap(BINDING_UI_DOWN.getGameBinding()),
                    GameInputStep.bindingTap(BINDING_UI_DOWN.getGameBinding()),
                    GameInputStep.bindingTap(BINDING_ACTIVATE.getGameBinding())
            ));
        } else if (status.isInMainShip()) {
            GameControllerBus.publish(GameInputSequenceEvent.of(
                    GameInputStep.bindingTap(BINDING_FOCUS_INTERNAL_PANEL.getGameBinding()),
                    GameInputStep.bindingTap(BINDING_UI_UP.getGameBinding()),
                    GameInputStep.delay(100),
                    GameInputStep.bindingTap(BINDING_UI_LEFT.getGameBinding()),
                    GameInputStep.delay(100),
                    GameInputStep.bindingTap(BINDING_UI_UP.getGameBinding()),
                    GameInputStep.delay(100),
                    GameInputStep.bindingTap(BINDING_UI_UP.getGameBinding()),
                    GameInputStep.delay(100),
                    GameInputStep.bindingTap(BINDING_UI_DOWN.getGameBinding()),
                    GameInputStep.delay(100),
                    GameInputStep.bindingTap(BINDING_ACTIVATE.getGameBinding())
            ));
        } else if (status.isInSrv()) {
            GameControllerBus.publish(GameInputSequenceEvent.of(
                    GameInputStep.bindingTap(BINDING_FOCUS_INTERNAL_PANEL_BUGGY.getGameBinding()),
                    GameInputStep.bindingTap(BINDING_UI_DOWN.getGameBinding()),
                    GameInputStep.bindingTap(BINDING_ACTIVATE.getGameBinding())
            ));
        }
    }
}

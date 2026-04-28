package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.*;

public class OpenFleetCarrierManagementHandler implements CommandHandler {


    private final Status status = Status.getInstance();

    /// not a sure fire. assumes default UI selection. will fail often.
    @Override public void handle(String action, JsonObject params, String responseText) {
        UiNavCommon.close();
        if(status.isOnFoot()){
            GameControllerBus.publish(new GameInputEvent(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_RIGHT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
        } else if(status.isInMainShip()){
            GameControllerBus.publish(new GameInputEvent(BINDING_FOCUS_INTERNAL_PANEL.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
        } else if(status.isInSrv()){
            GameControllerBus.publish(new GameInputEvent(BINDING_FOCUS_INTERNAL_PANEL_BUGGY.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(BINDING_ACTIVATE.getGameBinding(), 0));
        }
    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

public class DeployFighterHandler implements CommandHandler {

    private final Status status = Status.getInstance();



    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip()) {
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding(), 0));
            /// ensure the cursor is at the top
            for (int i = 0; i < 5; i++) {
                GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0));
            }
            for (int i = 0; i < 5; i++) {
                GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
            }

            /// Deploy Fighter
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
            SleepNoThrow.sleep(150);
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0));
            SleepNoThrow.sleep(150);
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_SELECT.getGameBinding(), 0));
            for (int i = 0; i < 6; i++) {
                GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
                SleepNoThrow.sleep(150);
            }
            for (int i = 0; i < 3; i++) {
                GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
                SleepNoThrow.sleep(150);
            }
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_SELECT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding(), 0));
            status.setOkToAnnounceLoadout(false);
        }
    }
}

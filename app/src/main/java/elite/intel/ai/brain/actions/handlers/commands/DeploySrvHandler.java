package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.Bindings;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.UINavigator;

public class DeploySrvHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {

        if (status.isInMainShip()) {
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding(), 0));

            /// ensure the cursor is at the top
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0));

            /// Deploy SRV
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_ACTIVATE.getGameBinding(), 0));
            navigator.assumeDefaultState(StatusFlags.GuiFocus.ROLE_PANEL);
        }
    }
}

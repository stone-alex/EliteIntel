package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.UINavigator;

public class RecoverSrvHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();

    @Override public void handle(String action, JsonObject params, String responseText) {
        GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL_BUGGY.getGameBinding(), 0));

        String ui_left = Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding();
        String ui_up = Bindings.GameCommand.BINDING_UI_UP.getGameBinding();
        String ui_down = Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding();
        String ui_right = Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding();
        String activate = Bindings.GameCommand.BINDING_ACTIVATE.getGameBinding();

        /// ensure the cursor is at the top

        GameControllerBus.publish(new GameInputEvent(ui_left, 0));
        GameControllerBus.publish(new GameInputEvent(ui_left, 0));
        GameControllerBus.publish(new GameInputEvent(ui_up, 0));
        GameControllerBus.publish(new GameInputEvent(ui_up, 0));
        GameControllerBus.publish(new GameInputEvent(ui_up, 0));
        ///
        GameControllerBus.publish(new GameInputEvent(ui_down, 0));
        GameControllerBus.publish(new GameInputEvent(ui_right, 0));
        GameControllerBus.publish(new GameInputEvent(activate, 0));
        navigator.assumeDefaultState(StatusFlags.GuiFocus.ROLE_PANEL);
    }
}

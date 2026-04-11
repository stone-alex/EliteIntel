package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.LeftPanel;
import elite.intel.session.ui.UINavigator;

public class RequestDockingHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        /// NOTE: This may fail often. the panel default initial state is assume to be "NAVIGATION" there is no way to verify
        /// NOTE: There is no way to detect where we are on the UI, or what is selected.
        if(status.isInMainShip()){
            navigator.assumeDefaultState(StatusFlags.GuiFocus.EXTERNAL_PANEL);
            // un-target ships

            navigator.openAndNavigate(StatusFlags.GuiFocus.EXTERNAL_PANEL, LeftPanel.CONTACTS);
            //navigate to panel
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 1500)); // scroll up to the top (and hope our station is there)
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0));

            //request docking
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_SELECT.getGameBinding(), 120));
            /// Exit
            navigator.closeAndRestore(StatusFlags.GuiFocus.EXTERNAL_PANEL);
        }
        if (status.isInFighter()) {
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_UI_SELECT.getGameBinding(), 120));
            GameControllerBus.publish(new GameInputEvent(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding(), 0));
        }
    }
}

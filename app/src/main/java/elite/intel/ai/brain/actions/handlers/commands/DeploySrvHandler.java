package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.Bindings;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.UINavigator;

public class DeploySrvHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {

        if (status.isInMainShip()) {
            GameControllerBus.publish(GameInputSequenceEvent.of(
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding()),
                    // Ensure the cursor is at the top before navigating to the SRV option.
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding()),
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding()),
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_UP.getGameBinding()),
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_UP.getGameBinding()),
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_UP.getGameBinding()),
                    // Deploy SRV.
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding()),
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding()),
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding()),
                    GameInputStep.bindingTap(Bindings.GameCommand.BINDING_ACTIVATE.getGameBinding())
            ));
            navigator.assumeDefaultState(StatusFlags.GuiFocus.ROLE_PANEL);
        }
    }
}

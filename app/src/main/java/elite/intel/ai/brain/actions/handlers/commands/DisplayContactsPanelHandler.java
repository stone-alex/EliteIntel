package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_FOCUS_CONTACTS_PANEL_BUGGY;
import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_FOCUS_LEFT_PANEL;

public class DisplayContactsPanelHandler implements CommandHandler {



    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInMainShip()) {
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_FOCUS_LEFT_PANEL.getGameBinding())));
        }

        if (status.isInSrv()) {
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_FOCUS_CONTACTS_PANEL_BUGGY.getGameBinding())));
        }
    }
}

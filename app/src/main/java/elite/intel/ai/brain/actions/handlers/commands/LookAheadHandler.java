package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_HEAD_LOOK_RESET;

public class LookAheadHandler implements CommandHandler {

    private final Status status = Status.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        if (status.isInMainShip()) {
            UiNavCommon.close();
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(BINDING_HEAD_LOOK_RESET.getGameBinding())));
        }
    }
}

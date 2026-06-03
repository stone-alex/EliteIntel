package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;

import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_EXPLORATION_FSSDISCOVERY_SCAN;
import static elite.intel.ai.hands.Bindings.GameCommand.BINDING_SET_SPEED_ZERO;

public class DisplayFssAndScanHandler implements CommandHandler {


    private final Status status = Status.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        if (status.isScoopingFuel()) {
            EventBusManager.publish(new AiVoxResponseEvent("We are scooping fuel, I can't do that right now."));
            return;
        }

        if (!status.isInSupercruise()) {
            EventBusManager.publish(new AiVoxResponseEvent("We must be in supercruise to do that."));
            return;
        }

        String stop = BINDING_SET_SPEED_ZERO.getGameBinding();
        String fssControl = BINDING_EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding();

        GameControllerBus.publish(GameInputSequenceEvent.of(
                GameInputStep.bindingTap(stop),
                GameInputStep.delay(200),
                GameInputStep.bindingTap(fssControl),
                GameInputStep.delay(1500),
                GameInputStep.bindingHold(fssControl, 4500)
        ));

    }
}

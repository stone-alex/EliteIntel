package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EXPLORATION_FSSDISCOVERY_SCAN;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_SET_SPEED_ZERO;

public class DisplayFssAndScanHandler implements CommandHandler {


    private final Status status = Status.getInstance();

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        if (status.isScoopingFuel()) {
            EventBusManager.publish(new AiVoxResponseEvent("We are scooping fuel, I can't do that right now."));
            return;
        }

        String stop = BINDING_SET_SPEED_ZERO.getGameBinding();
        String fssControl = BINDING_EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding();

        GameControllerBus.publish(new GameInputEvent(stop, 0));
        SleepNoThrow.sleep(200);

        GameControllerBus.publish(new GameInputEvent(fssControl, 0));
        SleepNoThrow.sleep(1500);
        GameControllerBus.publish(new GameInputEvent(fssControl, 4500));

    }
}

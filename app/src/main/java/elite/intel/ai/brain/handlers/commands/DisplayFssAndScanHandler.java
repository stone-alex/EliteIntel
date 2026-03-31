package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.events.GameInputEvent;
import elite.intel.gameapi.GameControllerBus;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EXPLORATION_FSSDISCOVERY_SCAN;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_SET_SPEED_ZERO;

public class DisplayFssAndScanHandler implements CommandHandler {



    @Override public void handle(String action, JsonObject params, String responseText) {

        try {
            String stop = BINDING_SET_SPEED_ZERO.getGameBinding();
            String fssControl = BINDING_EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding();

            GameControllerBus.publish(new GameInputEvent(stop, 0));
            Thread.sleep(200);

            GameControllerBus.publish(new GameInputEvent(fssControl, 0));
            Thread.sleep(1500);
            GameControllerBus.publish(new GameInputEvent(fssControl, 4500));

        } catch (InterruptedException e) {
            // ok
        }
    }
}

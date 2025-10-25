package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EXPLORATION_FSSDISCOVERY_SCAN;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_SET_SPEED_ZERO;

public class DisplayFssAndScanHandler extends CommandOperator implements CommandHandler {


    public DisplayFssAndScanHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        try {
            String stop = BINDING_SET_SPEED_ZERO.getGameBinding();
            String fssControl = BINDING_EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding();

            operateKeyboard(stop, 0);
            Thread.sleep(200);

            operateKeyboard(fssControl,0);
            Thread.sleep(1500);
            operateKeyboard(fssControl,4500);

        } catch (InterruptedException e) {
            // ok
        }
    }
}

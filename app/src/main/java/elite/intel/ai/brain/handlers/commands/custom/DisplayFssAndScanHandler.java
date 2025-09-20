package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.hands.GameHandler;

public class DisplayFssAndScanHandler extends CustomCommandOperator implements CommandHandler {


    public DisplayFssAndScanHandler(GameHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

        try {
            String stop = GameCommands.GameCommand.SET_SPEED_ZERO.getGameBinding();
            String fssControl = GameCommands.GameCommand.EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding();

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

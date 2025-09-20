package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.hands.GameHandler;

public class OpenFssHandler extends CustomCommandOperator implements CommandHandler {


    public OpenFssHandler(GameHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {
            String stop = GameCommands.GameCommand.SET_SPEED_ZERO.getGameBinding();
            String fssControl = GameCommands.GameCommand.EXPLORATION_FSSDISCOVERY_SCAN.getGameBinding();
            operateKeyboard(stop, 0);
            Thread.sleep(250);
            operateKeyboard(fssControl, 0);
        } catch (InterruptedException e) {
            // ok
        }
    }
}

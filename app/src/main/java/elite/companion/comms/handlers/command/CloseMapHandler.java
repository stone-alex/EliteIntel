package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.robot.GameCommandHandler;

public class CloseMapHandler extends CustomCommandOperator implements CommandHandler {


    public CloseMapHandler(GameCommandHandler commandHandler) throws Exception {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {
            String exit = CommandActionsGame.GameCommand.EXIT_KEY.getGameBinding();
            operateKeyboard(exit, 0);
            Thread.sleep(120);
            operateKeyboard(exit, 0);
            Thread.sleep(120);
            operateKeyboard(exit, 0);
            Thread.sleep(120);
            operateKeyboard(exit, 0);
            Thread.sleep(120);
            operateKeyboard(exit, 0);
            Thread.sleep(120);
        } catch (InterruptedException oops) {
            //ok
        }
    }
}

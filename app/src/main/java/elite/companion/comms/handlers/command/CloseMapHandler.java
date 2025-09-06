package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.brain.robot.GameCommandHandler;

/**
 * The CloseMapHandler class is responsible for executing the command to close the in-game map
 * by interacting with the game's keyboard bindings. This handler executes repeated key presses
 * to ensure the "close map" action is performed reliably.
 * <p>
 * This class extends the CustomCommandOperator to use its keyboard operation capabilities and
 * implements the CommandHandler interface to handle specific commands.
 */
public class CloseMapHandler extends CustomCommandOperator implements CommandHandler {


    public CloseMapHandler(GameCommandHandler commandHandler) {
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
            operateKeyboard(exit, 0); // exit from all sub windows back to HUD
        } catch (InterruptedException oops) {
            //ok
        }
    }
}

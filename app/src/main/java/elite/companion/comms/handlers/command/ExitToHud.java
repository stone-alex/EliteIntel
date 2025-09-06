package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.comms.brain.robot.GameCommandHandler;


/**
 * The ExitToHud class is responsible for handling the functionality required to navigate
 * out of nested menus and sub-windows in a game interface, ultimately returning to the
 * Heads-Up Display (HUD) of the game. It extends CustomCommandOperator and implements
 * the CommandHandler interface to define and execute the required sequence of commands.
 *
 * This functionality is achieved by simulating keyboard operations multiple times
 * in quick succession, using the provided key binding configurations.
 */
public class ExitToHud extends CustomCommandOperator implements CommandHandler {


    public static final int DELAY = 20;

    public ExitToHud(GameCommandHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        try {
            String exit = CommandActionsGame.GameCommand.EXIT_KEY.getGameBinding();
            String headLookReset = CommandActionsGame.GameCommand.HEAD_LOOK_RESET.getGameBinding();
            String quitFFS = CommandActionsGame.GameCommand.EXPLORATION_FSSQUIT.getGameBinding();
            //back out of nested menus and sub windows
            operateKeyboard(quitFFS, 0);
            Thread.sleep(DELAY);
            operateKeyboard(exit, 0);
            Thread.sleep(DELAY);
            operateKeyboard(exit, 0);
            Thread.sleep(DELAY);
            operateKeyboard(exit, 0);
            Thread.sleep(DELAY);
            operateKeyboard(exit, 0);
            Thread.sleep(DELAY);
            operateKeyboard(exit, 0);
            Thread.sleep(DELAY);
            operateKeyboard(exit, 0); // exit from all sub windows back to HUD
            Thread.sleep(DELAY);
            operateKeyboard(headLookReset, 0);
        } catch (InterruptedException oops) {
            //ok
        }
    }
}

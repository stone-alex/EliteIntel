package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.hands.GameHandler;


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


    public ExitToHud(GameHandler commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
            String exit = GameCommands.GameCommand.EXIT_KEY.getGameBinding();
            String headLookReset = GameCommands.GameCommand.HEAD_LOOK_RESET.getGameBinding();
            String quitFFS = GameCommands.GameCommand.EXPLORATION_FSSQUIT.getGameBinding();
            //back out of nested menus and sub windows
            
            operateKeyboard(quitFFS, 0);
            operateKeyboard(exit, 0);
            operateKeyboard(exit, 0);
            operateKeyboard(exit, 0);
            operateKeyboard(exit, 0);
            operateKeyboard(exit, 0);
            operateKeyboard(exit, 0);
            operateKeyboard(exit, 0);
            operateKeyboard(exit, 0); // exit from all sub windows back to HUD
            operateKeyboard(headLookReset, 0);
    }
}

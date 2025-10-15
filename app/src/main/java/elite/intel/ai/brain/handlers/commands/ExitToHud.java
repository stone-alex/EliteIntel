package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;


/**
 * The ExitToHud class is responsible for handling the functionality required to navigate
 * out of nested menus and sub-windows in a game interface, ultimately returning to the
 * Heads-Up Display (HUD) of the game. It extends CustomCommandOperator and implements
 * the CommandHandler interface to define and execute the required sequence of commands.
 * <p>
 * This functionality is achieved by simulating keyboard operations multiple times
 * in quick succession, using the provided key binding configurations.
 */
public class ExitToHud extends CustomCommandOperator implements CommandHandler {


    public ExitToHud(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        String exit = Bindings.GameCommand.BINDING_EXIT_KEY.getGameBinding();
        String headLookReset = Bindings.GameCommand.BINDING_HEAD_LOOK_RESET.getGameBinding();
        String quitFFS = Bindings.GameCommand.BINDING_EXPLORATION_FSSQUIT.getGameBinding();
        //back out of nested menus and sub windows

        operateKeyboard(quitFFS, 0);
        operateKeyboard(exit, 0);
        operateKeyboard(headLookReset, 0);
        operateKeyboard(exit, 0);
        operateKeyboard(headLookReset, 0);
        operateKeyboard(exit, 0);
        operateKeyboard(headLookReset, 0);
        operateKeyboard(exit, 0);
        operateKeyboard(headLookReset, 0);
        operateKeyboard(exit, 0);
        operateKeyboard(headLookReset, 0);
        operateKeyboard(exit, 0);
        operateKeyboard(headLookReset, 0);
        operateKeyboard(exit, 0);
        operateKeyboard(headLookReset, 0);
        operateKeyboard(exit, 0); // exit from all sub windows back to HUD
        operateKeyboard(headLookReset, 0);
    }
}

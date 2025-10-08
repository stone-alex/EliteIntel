package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.hands.GameController;

public class DeploySrvHandler extends CustomCommandOperator implements CommandHandler {

    public DeploySrvHandler(GameController handler) {
        super(handler.getMonitor(), handler.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {

        String openPanel = GameCommands.GameCommand.FOCUS_LOADOUT_PANEL.getGameBinding();

        String ui_left = GameCommands.GameCommand.UI_LEFT.getGameBinding();
        String ui_up = GameCommands.GameCommand.UI_UP.getGameBinding();
        String ui_down = GameCommands.GameCommand.UI_DOWN.getGameBinding();
        String ui_right = GameCommands.GameCommand.UI_RIGHT.getGameBinding();
        String activate = GameCommands.GameCommand.UI_ACTIVATE.getGameBinding();

        /// ensure the cursor is at the top
        operateKeyboard(openPanel, 0);
        operateKeyboard(ui_left, 0);
        operateKeyboard(ui_left, 0);
        operateKeyboard(ui_up, 0);
        operateKeyboard(ui_up, 0);
        operateKeyboard(ui_up, 0);
        ///
        operateKeyboard(ui_down, 0);
        operateKeyboard(ui_down, 0);
        operateKeyboard(ui_right, 0);
        operateKeyboard(activate, 0);
    }
}

package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.ControllerBindings;
import elite.intel.ai.hands.GameController;

public class BoardSrvHandler extends CustomCommandOperator implements CommandHandler {

    public BoardSrvHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        String openPanel = ControllerBindings.GameCommand.BINDING_FOCUS_LOADOUT_PANEL.getGameBinding();

        String ui_left = ControllerBindings.GameCommand.BINDING_UI_LEFT.getGameBinding();
        String ui_up = ControllerBindings.GameCommand.BINDING_UI_UP.getGameBinding();
        String ui_down = ControllerBindings.GameCommand.BINDING_UI_DOWN.getGameBinding();
        String ui_right = ControllerBindings.GameCommand.BINDING_UI_RIGHT.getGameBinding();
        String activate = ControllerBindings.GameCommand.BINDING_ACTIVATE.getGameBinding();

        /// ensure the cursor is at the top
        operateKeyboard(openPanel, 0);
        operateKeyboard(ui_left, 0);
        operateKeyboard(ui_left, 0);
        operateKeyboard(ui_up, 0);
        operateKeyboard(ui_up, 0);
        operateKeyboard(ui_up, 0);
        ///
        operateKeyboard(ui_down, 0);
        operateKeyboard(ui_right, 0);
        operateKeyboard(activate, 0);

    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.CenterPanel;
import elite.intel.session.ui.UINavigator;

public class DeploySrvHandler extends CommandOperator implements CommandHandler {

    public DeploySrvHandler(GameController handler) {
        super(handler.getMonitor(), handler.getExecutor());
    }

    private final UINavigator navigator = new UINavigator(this);
    private final Status status = Status.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {

        if (status.isInMainShip()) {
            operateKeyboard(Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0);
            navigator.openAndNavigate(StatusFlags.GuiFocus.ROLE_PANEL, CenterPanel.COMMANDER);
            /// NOTE: Special case here. DO NOT use navigator to get to the SRV tab.
            /// After SRV deployed, we have no chance to close the panel and return to the default state.

            String ui_left = Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding();
            String ui_up = Bindings.GameCommand.BINDING_UI_UP.getGameBinding();
            String ui_down = Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding();
            String ui_right = Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding();
            String activate = Bindings.GameCommand.BINDING_ACTIVATE.getGameBinding();

            /// ensure the cursor is at the top
            operateKeyboard(ui_left, 0);
            operateKeyboard(ui_left, 0);
            operateKeyboard(ui_up, 0);
            operateKeyboard(ui_up, 0);
            operateKeyboard(ui_up, 0);

            /// Deploy SRV
            operateKeyboard(ui_down, 0);
            operateKeyboard(ui_down, 0);
            operateKeyboard(ui_right, 0);
            operateKeyboard(activate, 0);
            navigator.assumeDefaultState(StatusFlags.GuiFocus.ROLE_PANEL);
        }
    }
}

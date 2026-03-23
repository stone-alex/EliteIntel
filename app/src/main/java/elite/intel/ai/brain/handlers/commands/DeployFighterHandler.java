package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;
import elite.intel.util.SleepNoThrow;

public class DeployFighterHandler extends CommandOperator implements CommandHandler {

    private final Status status = Status.getInstance();


    public DeployFighterHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip()) {
            operateKeyboard(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding(), 0);
            /// ensure the cursor is at the top
            for (int i = 0; i < 5; i++) {
                operateKeyboard(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0);
            }
            for (int i = 0; i < 5; i++) {
                operateKeyboard(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0);
            }

            /// Deploy Fighter
            operateKeyboard(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0);
            SleepNoThrow.sleep(150);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0);
            SleepNoThrow.sleep(150);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_SELECT.getGameBinding(), 0);
            //SleepNoThrow.sleep(2000);
            for (int i = 0; i < 4; i++) {
                operateKeyboard(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0);
                SleepNoThrow.sleep(150);
            }
            for (int i = 0; i < 3; i++) {
                operateKeyboard(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0);
                SleepNoThrow.sleep(150);
            }
            operateKeyboard(Bindings.GameCommand.BINDING_UI_SELECT.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_LEFT.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_FOCUS_ROLE_PANEL.getGameBinding(), 0);
        }
    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_UI_RIGHT;

public class OpenFleetCarrierManagementHandler extends CommandOperator implements CommandHandler {

    public OpenFleetCarrierManagementHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    private final Status status = Status.getInstance();

    /// not a sure fire. assumes default UI selection. will fail often.
    @Override public void handle(String action, JsonObject params, String responseText) {
        if(status.isOnFoot()){
            operateKeyboard(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500);
            operateKeyboard(BINDING_UI_RIGHT.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(BINDING_ACTIVATE.getGameBinding(), 0);
        } else if(status.isInMainShip()){
            operateKeyboard(BINDING_FOCUS_INTERNAL_PANEL.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(BINDING_ACTIVATE.getGameBinding(), 0);
        } else if(status.isInSrv()){
            operateKeyboard(BINDING_FOCUS_INTERNAL_PANEL_BUGGY.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(BINDING_ACTIVATE.getGameBinding(), 0);
        }
    }
}

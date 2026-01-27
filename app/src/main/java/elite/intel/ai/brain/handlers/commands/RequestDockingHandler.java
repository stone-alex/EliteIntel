package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

public class RequestDockingHandler extends CommandOperator implements CommandHandler {

    public RequestDockingHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    private final Status status = Status.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {
        /// NOTE: This may fail often. the panel initial state is assume to be "NAVIGATION" there is no way to verify
        if(status.isInMainShip()){
            //navigate to panel
            operateKeyboard(Bindings.GameCommand.BINDING_FOCUS_CONTACTS_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0);

            //request docking
            operateKeyboard(Bindings.GameCommand.BINDING_UI_SELECT.getGameBinding(), 0);

            //navigate out
            operateKeyboard(Bindings.GameCommand.BINDING_CYCLE_NEXT_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_CYCLE_NEXT_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_FOCUS_CONTACTS_PANEL.getGameBinding(), 0);
        }
    }
}

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
        /// NOTE: There is no way to detect where we are on the UI, or what is selected.
        if(status.isInMainShip()){

            // un-target ships
            operateKeyboard(Bindings.GameCommand.BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), 0);

            //navigate to panel
            operateKeyboard(Bindings.GameCommand.BINDING_FOCUS_CONTACTS_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_DOWN.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_UI_UP.getGameBinding(), 1500); // scroll up to the top (and hope our station is there)
            operateKeyboard(Bindings.GameCommand.BINDING_UI_RIGHT.getGameBinding(), 0);

            //request docking
            operateKeyboard(Bindings.GameCommand.BINDING_UI_SELECT.getGameBinding(), 120);

            //navigate out reset back to navigation panel.
            operateKeyboard(Bindings.GameCommand.BINDING_CYCLE_NEXT_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_CYCLE_NEXT_PANEL.getGameBinding(), 0);
            operateKeyboard(Bindings.GameCommand.BINDING_FOCUS_CONTACTS_PANEL.getGameBinding(), 0);
        }
    }
}

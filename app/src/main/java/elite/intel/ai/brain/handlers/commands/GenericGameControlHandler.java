package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;


public class GenericGameControlHandler extends CommandOperator implements CommandHandler {

    private final Status status = Status.getInstance();

    public GenericGameControlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {

        if (status.isInSrv()) {
            if (action != null) {
                operateKeyboard(Commands.getGameBinding(action), 0);
            }
        } else if (status.isOnFoot()) {
            operateKeyboard(BINDING_ON_FOOT_WHEEL.getGameBinding(), 500);
            operateKeyboard(BINDING_UI_LEFT.getGameBinding(), 0);
            operateKeyboard(BINDING_UI_UP.getGameBinding(), 0);
            operateKeyboard(BINDING_ACTIVATE.getGameBinding(), 0);
        }
    }

}

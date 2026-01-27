package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;


public class GenericGameControlHandler extends CommandOperator implements CommandHandler {

    private final Status status = Status.getInstance();

    public GenericGameControlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        if (action != null) {
            operateKeyboard(Commands.getGameBinding(action), 0);
        }
    }

}

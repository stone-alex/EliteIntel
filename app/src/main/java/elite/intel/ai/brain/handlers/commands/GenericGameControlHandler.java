package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;


public class GenericGameControlHandler extends CommandOperator implements CommandHandler {

    public GenericGameControlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        if (action != null) operateKeyboard(Commands.getGameBinding(action), 0);
    }

}

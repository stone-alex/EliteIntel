package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_EXIT_SUPERCRUISE;

public class ExitFtlHandler extends CustomCommandOperator implements CommandHandler {

    public ExitFtlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInSupercruise()) {
            operateKeyboard(BINDING_EXIT_SUPERCRUISE.getGameBinding(), 0);
        }
    }
}

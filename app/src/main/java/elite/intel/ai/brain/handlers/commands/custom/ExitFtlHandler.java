package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.session.Status;

import static elite.intel.ai.brain.handlers.commands.GameCommands.GameCommand.EXIT_SUPERCRUISE;

public class ExitFtlHandler extends CustomCommandOperator implements CommandHandler {

    public ExitFtlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(JsonObject params, String responseText) {
        Status status = Status.getInstance();

        if (status.isInSupercruise()) {
            operateKeyboard(EXIT_SUPERCRUISE.getGameBinding(), 0);
        }
    }
}

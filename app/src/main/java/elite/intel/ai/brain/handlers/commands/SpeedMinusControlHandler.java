package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;

public class SpeedMinusControlHandler extends CommandOperator implements CommandHandler {

    public SpeedMinusControlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        int num = params.get("key").getAsInt();
        String decrease = Commands.DECREASE_SPEED_BY.getBinding();
        for (int i = 0; i < num; i++) {
            operateKeyboard(decrease, 0);
        }
    }
}

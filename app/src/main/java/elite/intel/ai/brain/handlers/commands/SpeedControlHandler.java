package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;

import static elite.intel.ai.brain.handlers.commands.Commands.INCREASE_SPEED_BY;

public class SpeedControlHandler extends CustomCommandOperator implements CommandHandler {

    public SpeedControlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        int num = params.get("key").getAsInt();
        String increase = INCREASE_SPEED_BY.getBinding();
        String decrease = Commands.DECREASE_SPEED_BY.getBinding();

        if(num > 0) {
            for (int i = 0; i < num; i++) {
                operateKeyboard(increase, 0);
            }
        }
        if(num < 0) {
            for (int i = 0; i > num; i--) {
                operateKeyboard(decrease, 0);
            }
        }

    }
}

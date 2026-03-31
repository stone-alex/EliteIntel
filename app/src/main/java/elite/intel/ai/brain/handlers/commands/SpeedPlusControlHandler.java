package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.util.AudioPlayer;

import static elite.intel.ai.brain.handlers.commands.Commands.INCREASE_SPEED_BY;

public class SpeedPlusControlHandler extends CommandOperator implements CommandHandler {

    public SpeedPlusControlHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        int num = params.get("key").getAsInt();
        String increase = INCREASE_SPEED_BY.getBinding();
        for (int i = 0; i < num; i++) {
            operateKeyboard(increase, 0);
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        }


    }
}

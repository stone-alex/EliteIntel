package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.actions.Commands;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.util.AudioPlayer;

public class SpeedMinusControlHandler implements CommandHandler {


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        int num = params.get("key").getAsInt();
        String decrease = Commands.DECREASE_SPEED_BY.getBinding();
        for (int i = 0; i < num; i++) {
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(decrease)));
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        }
    }
}

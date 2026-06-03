package elite.intel.ai.brain.actions.handlers.commands;

import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;

import com.google.gson.JsonObject;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.util.AudioPlayer;

import static elite.intel.ai.brain.actions.Commands.INCREASE_SPEED_BY;

public class SpeedPlusControlHandler implements CommandHandler {


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        int num = params.get("key").getAsInt();
        String increase = INCREASE_SPEED_BY.getBinding();
        for (int i = 0; i < num; i++) {
            GameControllerBus.publish(GameInputSequenceEvent.single(GameInputStep.bindingTap(increase)));
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
        }


    }
}

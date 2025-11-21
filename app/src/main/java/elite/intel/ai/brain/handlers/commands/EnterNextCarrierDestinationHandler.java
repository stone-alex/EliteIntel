package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.hands.KeyProcessor;
import elite.intel.ai.search.spansh.carrierroute.CarrierJump;
import elite.intel.db.FleetCarrierRoute;
import elite.intel.util.AudioPlayer;
import elite.intel.util.SleepNoThrow;

import java.util.Collections;
import java.util.Map;

public class EnterNextCarrierDestinationHandler extends CommandOperator implements CommandHandler {


    public EnterNextCarrierDestinationHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }


    @Override public void handle(String action, JsonObject params, String responseText) {
        Map<Integer, CarrierJump> fleetCarrierRoute = FleetCarrierRoute.getInstance().getFleetCarrierRoute();

        if (!fleetCarrierRoute.isEmpty()) {
            Integer nextLeg = Collections.min(fleetCarrierRoute.keySet());
            CarrierJump carrierJump = fleetCarrierRoute.get(nextLeg);
            KeyProcessor keyProcessor = KeyProcessor.getInstance();
            if(carrierJump.getSystemName() != null) {
                keyProcessor.enterText(carrierJump.getSystemName());
                SleepNoThrow.sleep(250);
                keyProcessor.pressKey(KeyProcessor.KEY_ENTER);
                keyProcessor.pressKey(KeyProcessor.KEY_ENTER);
                AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
            }
        }
    }
}

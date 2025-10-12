package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.hands.KeyProcessor;
import elite.intel.ai.search.spansh.carrier.CarrierJump;
import elite.intel.session.PlayerSession;
import elite.intel.util.AudioPlayer;
import elite.intel.util.SleepNoThrow;

import java.util.Collections;
import java.util.Map;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE;

public class EnterNextCarrierDestinationHandler extends CustomCommandOperator implements CommandHandler {


    public EnterNextCarrierDestinationHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }


    @Override public void handle(String action, JsonObject params, String responseText) {
        Map<Integer, CarrierJump> fleetCarrierRoute = PlayerSession.getInstance().getFleetCarrierRoute();

        if (!fleetCarrierRoute.isEmpty()) {
            Integer nextLeg = Collections.min(fleetCarrierRoute.keySet());
            CarrierJump carrierJump = fleetCarrierRoute.get(nextLeg);
            KeyProcessor keyProcessor = KeyProcessor.getInstance();
            if(carrierJump.getSystemName() != null) {
                keyProcessor.enterText(carrierJump.getSystemName());
                SleepNoThrow.sleep(250);
                keyProcessor.pressKey(KeyProcessor.KEY_ENTER);
                keyProcessor.pressKey(KeyProcessor.KEY_ENTER);
                AudioPlayer.getInstance().playBeep();
            }
        }
    }
}

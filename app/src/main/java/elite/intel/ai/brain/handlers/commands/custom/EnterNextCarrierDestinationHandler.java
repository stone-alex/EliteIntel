package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.hands.KeyProcessor;
import elite.intel.ai.search.spansh.carrier.CarrierJump;
import elite.intel.session.PlayerSession;

import java.util.Collections;
import java.util.Map;

public class EnterNextCarrierDestinationHandler extends CustomCommandOperator implements CommandHandler {


    public EnterNextCarrierDestinationHandler(GameController commandHandler) {
        super(commandHandler.getMonitor(), commandHandler.getExecutor());
    }


    @Override public void handle(JsonObject params, String responseText) {
        Map<Integer, CarrierJump> fleetCarrierRoute = PlayerSession.getInstance().getFleetCarrierRoute();

        if (!fleetCarrierRoute.isEmpty()) {
            Integer nextLeg = Collections.min(fleetCarrierRoute.keySet());
            CarrierJump carrierJump = fleetCarrierRoute.get(nextLeg);
            KeyProcessor keyProcessor = KeyProcessor.getInstance();
            if(carrierJump.getSystemName() != null) {
                keyProcessor.enterText(carrierJump.getSystemName());
            }
        }
    }
}

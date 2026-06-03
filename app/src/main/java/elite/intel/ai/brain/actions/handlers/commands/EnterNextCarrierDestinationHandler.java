package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.KeyProcessor;
import elite.intel.ai.hands.events.GameInputSequenceEvent;
import elite.intel.ai.hands.events.GameInputStep;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.gameapi.GameControllerBus;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.util.AudioPlayer;

import java.util.Collections;
import java.util.Map;

public class EnterNextCarrierDestinationHandler implements CommandHandler {




    @Override public void handle(String action, JsonObject params, String responseText) {
        Map<Integer, CarrierJump> fleetCarrierRoute = FleetCarrierRouteManager.getInstance().getFleetCarrierRoute();

        if (!fleetCarrierRoute.isEmpty()) {
            Integer nextLeg = Collections.min(fleetCarrierRoute.keySet());
            CarrierJump carrierJump = fleetCarrierRoute.get(nextLeg);
            if(carrierJump.getSystemName() != null) {
                GameControllerBus.publish(GameInputSequenceEvent.of(
                        GameInputStep.text(carrierJump.getSystemName()),
                        GameInputStep.delay(250),
                        GameInputStep.rawKey(KeyProcessor.KEY_ENTER)
                ));
                AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2);
            }
        }
    }
}

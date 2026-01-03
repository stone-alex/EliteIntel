package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.util.FleetCarrierRouteCalculator;

public class CalculateFleetCarrierRouteHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        FleetCarrierRouteCalculator.calculate();
    }
}

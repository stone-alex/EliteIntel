package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.db.managers.FleetCarrierManager;

public class AnalyzeFleetCarrierFuelSupplyHandler extends BaseQueryAnalyzer implements QueryHandler {



    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        FleetCarrierManager fleetCarrierManager = FleetCarrierManager.getInstance();
        int fuelSupply = fleetCarrierManager.get().getFuelLevel();
        int fuelReserve = fleetCarrierManager.get().getFuelReserve();

        return process("Fuel supply: " + fuelSupply + " + " + fuelReserve);
    }
}

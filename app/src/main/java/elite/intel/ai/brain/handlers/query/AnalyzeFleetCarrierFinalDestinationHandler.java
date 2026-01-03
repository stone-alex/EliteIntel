package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.db.managers.FleetCarrierRouteManager;

public class AnalyzeFleetCarrierFinalDestinationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        FleetCarrierRouteManager manager = FleetCarrierRouteManager.getInstance();
        String finalDestination = manager.getFinalDestination();
        return process(finalDestination == null ? "Carrier Route is not set" : finalDestination);
    }
}

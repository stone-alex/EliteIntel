package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.db.managers.FleetCarrierRouteManager;

public class ClearFleetCarrierRouteHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        FleetCarrierRouteManager.getInstance().clear();
    }
}

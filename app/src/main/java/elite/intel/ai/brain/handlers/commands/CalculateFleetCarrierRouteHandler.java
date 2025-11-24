package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.search.spansh.carrierroute.CarrierRouteCriteria;
import elite.intel.search.spansh.carrierroute.SpanshCarrierRouteClient;
import elite.intel.search.spansh.nearest.NearestKnownLocationSearchClient;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.ClipboardUtils;
import elite.intel.util.FleetCarrierRouterCalculator;

import java.util.Map;

public class CalculateFleetCarrierRouteHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        FleetCarrierRouterCalculator.calculate();
    }
}

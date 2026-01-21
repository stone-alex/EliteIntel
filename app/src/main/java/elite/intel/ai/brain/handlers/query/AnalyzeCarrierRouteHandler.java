package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.FleetCarrierManager;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Map;

public class AnalyzeCarrierRouteHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing fleet carrier route... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        FleetCarrierManager fleetCarrierManager = FleetCarrierManager.getInstance();
        FleetCarrierRouteManager fleetCarrierRouteManager = FleetCarrierRouteManager.getInstance();
        Map<Integer, CarrierJump> fleetCarrierRoute = fleetCarrierRouteManager.getFleetCarrierRoute();
        CarrierDataDto carrierData = playerSession.getCarrierData();
        Integer fuelRequired = fleetCarrierRouteManager.getTotalFuelRequired();
        Integer fuelReserve = fleetCarrierManager.get().getFuelReserve();


        if (carrierData == null || fleetCarrierRoute == null) {
            return process("No data available");
        }

        int fuelSupply = carrierData.getFuelLevel() + fuelReserve;

        String instructions = """
                Use the provided route data to answer user questions: reference systemName for locations. 
                Identify refuel stops as those with hasIcyRing=true.
                For relevant queries, return number of jumps to final destination, jumps to nearest icy ring stop, and fuel required (1 unit = 1 ton). 
                Parse jump duration (e.g., '20 minutes') from original user query if mentioned.
                For ETA, calculate total time as jumps_to_destination * parsed_duration, format appropriately (e.g., 'X minutes' or 'Y hours X minutes'). 
                Include in response only if asked. 
                Compare currentFuelSupply to fuelRequired. Return delta if fuelRequried > currentFuelSupply. 
                Respond only with the specific info requested in a short, consistent manner without broad data dumps.
                """;
        return process(new AiDataStruct(instructions, new DataDto(fleetCarrierRoute, fuelSupply, fuelRequired)), originalUserInput);
    }


    private record DataDto(Map<Integer, CarrierJump> route, int currentFuelSupply, Integer fuelRequired) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

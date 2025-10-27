package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.search.spansh.carrier.CarrierJump;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Map;

public class AnalyzeCarrierRouteHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();

        Map<Integer, CarrierJump> fleetCarrierRoute = playerSession.getFleetCarrierRoute();
        CarrierDataDto carrierData = playerSession.getCarrierData();

        if (carrierData == null || fleetCarrierRoute == null) {
            return process("No data available");
        }

        int fuelSupply = carrierData.getFuelSupply() + carrierData.getFuelReserve();

        String instructions = "Use the provided route data to answer user questions: reference systemName for locations; identify refuel stops as those with hasIcyRing=true; for relevant queries, return number of jumps to final destination, jumps to nearest icy ring stop, and fuel required (1 unit = 1 ton); Parse jump duration (e.g., '20 minutes') from original user query if mentioned; for ETA, calculate total time as jumps_to_destination * parsed_duration, format appropriately (e.g., 'X minutes' or 'Y hours X minutes'); include in response only if asked., fuel levels, refuel places, etc., responding only with the specific info requested in a short, consistent manner without broad data dumps.";
        return process(new AiDataStruct(instructions, new DataDto(fleetCarrierRoute, fuelSupply)), originalUserInput);
    }


    private record DataDto(Map<Integer, CarrierJump> route, int currentFuelSupply) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

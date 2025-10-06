package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
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
        int fuelSupply = carrierData.getFuelSupply();
        if (fleetCarrierRoute == null) {
            return analyzeData(toJson("No data available"), originalUserInput);
        }

        String instructions = "Analyze carrier route. Look for stops with ice rings, that is where we can refuel.";

        return analyzeData(new DataDto(fleetCarrierRoute, fuelSupply, instructions).toJson(), originalUserInput);
    }


    private record DataDto(Map<Integer, CarrierJump> route, int currentFuelSupply, String instructions) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

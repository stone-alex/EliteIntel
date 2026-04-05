package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AnalyzeFleetCarrierFinalDestinationHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        FleetCarrierRouteManager manager = FleetCarrierRouteManager.getInstance();

        Map<Integer, CarrierJump> carrierRoute = manager.getFleetCarrierRoute();

        String instructions = """
                    Answer the user's question about the fleet carrier's route.
                    - 1 jump takes 20 minutes.
                    - fuel required for the journey is a sum of fuelRequiredForJump in each leg.
                    provide summary of the route, number of jumps, fuel required and estimated time to arrival.
                """;

        return process(new AiDataStruct(instructions, new DataDto(toData(carrierRoute))), originalUserInput);
    }

    private List<CarrierRouteLeg> toData(Map<Integer, CarrierJump> carrierRoute) {
        ArrayList<CarrierRouteLeg> result = new ArrayList<>();
        for (Map.Entry<Integer, CarrierJump> entry : carrierRoute.entrySet()) {
            result.add(new CarrierRouteLeg(
                    entry.getKey(),
                    entry.getValue().getSystemName(),
                    (int) entry.getValue().getDistance(),
                    entry.getValue().getFuelUsed(), entry.getValue().getRemainingFuel(), entry.getValue().getHasIcyRing())
            );
        }
        return result;
    }

    record DataDto(List<CarrierRouteLeg> carrierRoute) implements ToYamlConvertable {

        @Override
        public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }


    record CarrierRouteLeg(int legNumber,
                           String systemName,
                           int distance,
                           int fuelRequiredForJump,
                           int fuelRemainAfterJump,
                           boolean stopHasIcyRing
    ) implements ToYamlConvertable {

        @Override
        public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    ;


}

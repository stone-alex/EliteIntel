package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.FleetCarrierManager;
import elite.intel.db.managers.FleetCarrierRouteManager;
import elite.intel.search.spansh.carrierroute.CarrierJump;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.Map;


public class AnalyzeFleetCarrierFinalDestinationHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final FleetCarrierRouteManager manager = FleetCarrierRouteManager.getInstance();
    private final FleetCarrierManager carrierManager = FleetCarrierManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {


        Map<Integer, CarrierJump> carrierRoute = manager.getFleetCarrierRoute();
        Integer totalFuelRequired = manager.getTotalFuelRequired();
        Integer timeInMinutes = carrierRoute.size() * 20;
        Integer numJumps = carrierRoute.size();
        String finalDestination = manager.getFinalDestination();

        int fuelReserve = carrierManager.get().getFuelReserve();
        int fuelLevel = carrierManager.get().getFuelLevel();
        int fuelBalance = (fuelLevel + fuelReserve) - totalFuelRequired;
        String iceRingStops = calculateIceRingStops(carrierRoute);

        String instructions = """
                    Provide route summary.
                    DATA:
                    - finalDestination is destination
                    - timeInMinutes announce as hours and minutes
                    - totalFuelRequired is total fuel required
                    - numJumps is number of legs/stops/jumps
                    - refuelLocations are star systems with ice rings containing fuel
                    RULE:
                    Begin with: "The fleet carrier will arrive at [finalDestination] in HH hours and MM minutes. [totalFuelRequired] tons of fuel required to complete the journey."
                    Then evaluate fuelBalance from the data:
                    - If fuelBalance < 0 (negative): append "The carrier will need to refuel before reaching [finalDestination]."
                    - If fuelBalance >= 0 (zero or positive): append "The carrier will have sufficient fuel to reach [finalDestination]."
                    Do not add anything else.
                """;

        return process(new AiDataStruct(instructions, new DataDto(finalDestination, totalFuelRequired, fuelBalance, timeInMinutes, numJumps, iceRingStops)), originalUserInput);
    }

    private String calculateIceRingStops(Map<Integer, CarrierJump> carrierRoute) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ice ring stop at ");
        carrierRoute.forEach((k, v) -> {
            if (v.getHasIcyRing()) {
                sb.append(v.getSystemName()).append(",");
            }
        });
        return sb.toString().substring(0, sb.length() - 1);
    }

    record DataDto(String finalDestination, int fuelRequired, int fuelBalance, int timeInMinutes, int numJumps,
                   String refuelLocations) implements ToYamlConvertable {

        @Override
        public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }


}

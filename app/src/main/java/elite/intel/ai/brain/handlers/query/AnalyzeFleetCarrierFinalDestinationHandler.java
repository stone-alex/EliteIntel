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
                Answer the user's question about the fleet carrier's planned route.
                
                Data fields:
                - finalDestination: the destination system
                - timeInMinutes: total travel time — announce as hours and minutes
                - totalFuelRequired: tritium needed for the full journey in tons
                - numJumps: number of legs/stops/jumps to destination
                - refuelLocations: star systems with ice rings where the carrier can refuel
                - fuelBalance: positive = tritium surplus in tons, negative = shortfall in tons
                
                Rules:
                - Answer only what was asked. Do not volunteer unrequested data.
                - For destination questions: use finalDestination.
                - For ETA or travel time questions: use timeInMinutes.
                - For jump count questions: use numJumps.
                - For fuel questions: use totalFuelRequired and fuelBalance.
                - For refuel stop questions: use refuelLocations.
                - Only mention fuelBalance if the user asked about fuel or range.
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

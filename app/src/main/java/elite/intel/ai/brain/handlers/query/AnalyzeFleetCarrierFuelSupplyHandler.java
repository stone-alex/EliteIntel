package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.FleetCarrierManager;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzeFleetCarrierFuelSupplyHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        FleetCarrierManager fleetCarrierManager = FleetCarrierManager.getInstance();
        int fuelSupply = fleetCarrierManager.get().getFuelLevel();
        int fuelReserve = fleetCarrierManager.get().getFuelReserve();
        int maxRangeOnCurrentSupply = 500 * (fuelReserve / 100);
        int maxRangeUsingReserve = 500 * ((fuelReserve + fuelReserve) / 100);
        String instructions = """
                Fleet Carrier uses Tritium as fuel. It can travel 500 light years per ~100 tons of fuel.
                Use this data to answer questions about the fleet carrier's fuel supply, including current fuel level, fuel reserve,
                and estimated ranges based on current and combined fuel supplies. Range provided is in light years, fuel amount is provided in tonns.
                Spell out numerals.
                """;
        return process(new AiDataStruct(instructions, new DataDto(fuelSupply, fuelReserve, maxRangeOnCurrentSupply, maxRangeUsingReserve)), originalUserInput);
    }

    record DataDto(int fuelSupply, int fuelReserve, int maxRangeOnCurrentSupply, int maxRangeUsingReserve) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

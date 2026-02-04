package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzeCarrierDataHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing fleet carrier data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        CarrierDataDto stats = playerSession.getCarrierData();

        if (stats.getTotalBalance() == 0 && stats.getFuelLevel() == 0) {
            return process("No data available. Please open carrier management panel.");
        } else {
            String instructions = """
                    Range is in light years, max is calculated 'maxRange' value provided. 
                        - reserveBalance: credits reserved for weekly ops (usually thirty one million credits per week). 
                        - totalBalance: total credits in carrier bank, including reserveBalance. 
                        - marketBalance: credits for purchases; 
                        - fundedOperation: time in weeks the carrier can operate with current funding. 
                    Do not improvise or assume anything.
                    Example: Carrier balance is X credits. Or Carrier max range is X light years. Or We are funded for 19 weeks of ops.
                    Spell out numerals.
                    If data not available state so.
                    """;
            return process(
                    new AiDataStruct(
                            instructions,
                            new DataDto(
                                    stats.getReserveBalance(),
                                    stats.getTotalBalance(),
                                    stats.getMarketBalance(),
                                    stats.getFuelLevel(),
                                    stats.getFuelReserve(),
                                    (stats.getFuelLevel() + stats.getFuelReserve()),
                                    stats.getRange(),
                                    stats.getFundedOperation()
                            )

                    ),
                    originalUserInput
            );
        }
    }

    record DataDto(
            long reserveBalance,
            long totalBalance,
            long marketBalance,
            int fuelSupply,
            int fuelSupplyReserve,
            int totalFuelAvailable,
            int maxRange,
            int fundedOperation
    ) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

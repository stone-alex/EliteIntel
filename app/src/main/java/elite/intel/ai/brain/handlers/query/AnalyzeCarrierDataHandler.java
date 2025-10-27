package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCarrierDataHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        CarrierDataDto stats = playerSession.getCarrierData();

        if (stats.getTotalBalance() == 0 && stats.getFuelSupply() == 0) {
            return process("No data available. Please open carrier management panel.");
        } else {
            String instructions = "range is in light years, when asked about range use the maxRange value provided. reserveBalance: credits reserved for weekly ops (usually thirty one million credits per week). totalBalance: total credits in carrier bank, including reserveBalance. marketBalance: credits for purchases; negative means escrow for buys. X,Y,Z: light years from Sol (0,0,0). Do not improvise or assume anything. If data not available state so.";
            AiDataStruct struct = new AiDataStruct(instructions, new DataDto(stats, stats.getFuelSupply(), stats.getFuelReserve(), (stats.getFuelSupply() + stats.getFuelReserve()), stats.getRange()));
            return process(struct, originalUserInput);
        }
    }

    record DataDto(CarrierDataDto data, int fuelSupply, Integer fuelSupplyReserve, int totalFuelAvailable, int maxRange) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

    }
}

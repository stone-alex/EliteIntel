package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCarrierDataHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        CarrierDataDto stats = playerSession.getCarrierData();

        if (stats.getTotalBalance() == 0 && stats.getFuelSupply() == 0) {
            return analyzeData(toJson("No data available. Please open carrier management panel."), originalUserInput);
        } else {
            String instructions = "use this data: range is in light years, when asked about range use the maxRange value provided. reserveBalance: credits reserved for weekly ops (usually 31M/week). totalBalance: total credits in carrier bank, including reserveBalance. marketBalance: credits for purchases; negative means escrow for buys. X,Y,Z: light years from Sol (0,0,0). Do not improvise or assume anything. If data not available state so.";
            return analyzeData(new DataDto(stats, stats.getFuelSupply(), stats.getFuelReserve(), (stats.getFuelSupply() + stats.getFuelReserve()), stats.getRange(), instructions).toJson(), originalUserInput);
        }
    }

    record DataDto(CarrierDataDto data, int fuelSupply, Integer fuelSupplyReserve, int totalFuelAvailable, int maxRange, String instructions) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

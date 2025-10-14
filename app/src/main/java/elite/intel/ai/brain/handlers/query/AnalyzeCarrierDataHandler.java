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

        if (stats == null || (stats.getTotalBalance() == 0 && stats.getFuelSupply() == 0)) {
            return analyzeData(toJson("No data available. Please open carrier management panel."), originalUserInput);
        } else {
            String instructions = "use this data to answer questions about fleet carrier. The reserveBalance is amount of credits reserved for weekly operation expense (usually 31 million per week). totalBalance is amount of credits in fleet carrier bank, this includes the reserve balance. The marketBalance is amount of money allocated for purchases. The negative amount in marketBalance indicates escrow reserved for commodities that carrier wants to purchase. The X,Y,Z coordinates are in light years where 0,0,0 is Earth (bubble).";
            return analyzeData(new DataDto(stats, instructions).toJson(), originalUserInput);
        }
    }

    record DataDto(CarrierDataDto data, String instructions) implements ToJsonConvertible{
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

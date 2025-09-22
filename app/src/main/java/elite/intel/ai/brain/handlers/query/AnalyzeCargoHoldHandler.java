package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCargoHoldHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object cargo = playerSession.get(PlayerSession.SHIP_CARGO);
        String cargoAsString = cargo != null ? toJson(cargo) : "No cargo data available.";


        Object loadout = playerSession.get(PlayerSession.SHIP_LOADOUT_JSON);
        String loadoutAsString = loadout != null ? String.valueOf(loadout) : "Ship Loadout data is unavailable.";
        String data = new DataDto(loadoutAsString, cargoAsString).toJson();

        return analyzeData(data, originalUserInput);
    }


    record DataDto(String loadout, String cargo) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

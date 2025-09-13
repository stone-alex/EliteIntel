package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCargoHoldHandler extends BaseQueryAnalyzer implements QueryHandler {
    private static final Gson GSON = GsonFactory.getGson();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Object cargo = playerSession.get(PlayerSession.SHIP_CARGO);
        String cargoAsString = cargo != null ? GSON.toJson(cargo) : "No cargo data available.";


        Object loadout = playerSession.get(PlayerSession.SHIP_LOADOUT_JSON);
        String loadoutAsString = loadout != null ? String.valueOf(loadout) : "Ship Loadout data is unavailable.";
        String data = new DataDto(loadoutAsString, cargoAsString).toJson();

        return analyzeData(data, originalUserInput);
    }


    static class DataDto implements ToJsonConvertible {

        private String loadout;
        private String cargo;

        public DataDto(String loadout, String cargo) {
            this.loadout = loadout;
            this.cargo = cargo;
        }

        public String getLoadout() {
            return loadout;
        }

        public String getCargo() {
            return cargo;
        }

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

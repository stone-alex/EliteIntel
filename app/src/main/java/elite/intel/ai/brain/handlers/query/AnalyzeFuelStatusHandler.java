package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeFuelStatusHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Gson GSON = GsonFactory.getGson();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        //TODO: Convert info in to dtos, and write logic to figure out how much fuel is used per maximum range jump.
        PlayerSession playerSession = PlayerSession.getInstance();
        Object fuelStatusObject = playerSession.get(PlayerSession.CURRENT_FUEL_STATUS);
        String fuelStatusData = fuelStatusObject != null ? GSON.toJson(fuelStatusObject) : "No fuel status data available";

        Object loadout = playerSession.get(PlayerSession.SHIP_LOADOUT_JSON);
        String loadoutAsString = loadout != null ? String.valueOf(loadout) : "Ship Loadout data is unavailable.";
        String data = new AnalyzeCargoHoldHandler.DataDto(loadoutAsString, fuelStatusData).toJson();

        return analyzeData(data, originalUserInput);
    }


    static class DataDto implements ToJsonConvertible {
        private String shipLoadout;
        private String fuelData;

        public DataDto(String loadout, String fuelStatus) {
            this.shipLoadout = loadout;
            this.fuelData = fuelStatus;
        }

        public String getShipLoadout() {
            return shipLoadout;
        }

        public String getFuelData() {
            return fuelData;
        }

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

    }
}

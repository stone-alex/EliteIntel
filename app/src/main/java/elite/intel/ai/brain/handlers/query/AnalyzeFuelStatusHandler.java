package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeFuelStatusHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        //TODO: Convert info in to dtos, and write logic to figure out how much fuel is used per maximum range jump.
        PlayerSession playerSession = PlayerSession.getInstance();
        GameEvents.StatusEvent fuelStatus = playerSession.getFuelStatus();
        LoadoutEvent loadout = playerSession.getShipLoadout();
        if(loadout != null && fuelStatus != null) {
            return analyzeData(new DataDto(loadout, fuelStatus).toJson(), originalUserInput);
        } else if(loadout != null) {
            return analyzeData(new DataDto(loadout, null).toJson(), originalUserInput);
        }
        else {
            return analyzeData(toJson("Data not available"), originalUserInput);
        }
    }


    record DataDto(LoadoutEvent loadout, GameEvents.StatusEvent fuelData)  implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

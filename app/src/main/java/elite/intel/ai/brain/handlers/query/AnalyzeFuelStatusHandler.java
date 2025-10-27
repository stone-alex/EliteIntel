package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.json.GsonFactory;

public class AnalyzeFuelStatusHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        //TODO: Convert info in to dtos, and write logic to figure out how much fuel is used per maximum range jump.
        PlayerSession playerSession = PlayerSession.getInstance();
        Status status = Status.getInstance();
        GameEvents.StatusEvent fuelStatus =status.getStatus();
        LoadoutEvent loadout = playerSession.getShipLoadout();

        if(loadout != null && fuelStatus != null) {
            return process(new DataDto("Use loadout data and fuel fuelStatus.fuelMain to provide answers.", loadout, fuelStatus), originalUserInput);
        } else if(loadout != null) {
            return process(new DataDto("Use loadout data to provide answers.", loadout, null), originalUserInput);
        }
        else {
            return process("Data not available");
        }
    }


    record DataDto(String instructions, LoadoutEvent loadout, GameEvents.StatusEvent fuelData) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

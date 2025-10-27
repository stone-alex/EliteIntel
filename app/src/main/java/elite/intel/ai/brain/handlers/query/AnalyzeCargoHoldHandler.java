package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCargoHoldHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();

        String instructions = "Use this data provide questions regarding cargo and/or ship loadout if relevant. Cargo is listed 1 unit = 1 ton. ";

        AiDataStruct struct = new AiDataStruct(instructions, new DataDto(playerSession.getShipLoadout(), playerSession.getShipCargo()));

        return process(struct, originalUserInput);
    }


    record DataDto(LoadoutEvent loadout, GameEvents.CargoEvent cargo) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

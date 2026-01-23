package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.journal.events.dto.shiploadout.ShipLoadOutDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeCargoHoldHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing cargo data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();

        String instructions = """
                Use this data provide questions regarding cargo and/or ship loadout if relevant.
                Cargo is listed 1 unit = 1 ton. 
                    - **Do not** ask follow up questions, just provide information.
                """;
        return process(new AiDataStruct(instructions, new DataDto(playerSession.getShipLoadout().getCargoCapacity(), playerSession.getShipCargo())), originalUserInput);
    }

    record DataDto(int cargoCapacity, GameEvents.CargoEvent cargo) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

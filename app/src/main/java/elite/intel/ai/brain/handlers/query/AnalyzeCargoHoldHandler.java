package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.gameapi.journal.events.dto.shiploadout.ShipLoadOutDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_ANALYZE_ON_BOARD_CARGO;

public class AnalyzeCargoHoldHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing cargo data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();

        return process(new AiDataStruct(QUERY_ANALYZE_ON_BOARD_CARGO.getInstructions(), new DataDto(playerSession.getShipLoadout(), playerSession.getShipCargo())), originalUserInput);
    }

    record DataDto(ShipLoadOutDto loadout, GameEvents.CargoEvent cargo) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.search.edsm.dto.ShipyardDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_LOCAL_SHIPYARD;

public class AnalyzeShipyardHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing shipyard data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        ShipyardDto shipyard = playerSession.getCurrentLocation().getShipyard();

        return process(new AiDataStruct(ANALYZE_LOCAL_SHIPYARD.getInstructions(), new DataDto(shipyard)), originalUserInput);
    }

    private record DataDto(ToJsonConvertible shipyard) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeDistanceFromTheBubble extends BaseQueryAnalyzer implements QueryHandler{

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing galactic coordinates... Stand by..."));
        LocationDao.Coordinates galacticCoordinates = LocationManager.getInstance().getGalacticCoordinates();

        if (galacticCoordinates.x() == 0 && galacticCoordinates.y() == 0 && galacticCoordinates.z() == 0) {
            return process("Local Coordinates are not available.");
        }

        String instruction = """
                Calculate distance to the bubble.
                    - Center of the bubble (Earth) is at coordinates 0, 0, 0. 
                    - Use the coordinates provided to calculate the distance from the bubble.
                    - Return answer as whole number. The distance is in light years. 
                    - IF asked about amount of fleet carrier fuel needed to cover the distance use 90 tons of fuel per 500 light year jump to calculate the amount.
                    Example response {"type":"chat", "response_text","X light years, carrier fuel required N tons."}
                """;
        return process(new AiDataStruct(instruction, new DataDto(galacticCoordinates)), originalUserInput);
    }

    record DataDto(LocationDao.Coordinates galacticCoordinates) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

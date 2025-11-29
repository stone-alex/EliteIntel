package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeDistanceFromTheBubble extends BaseQueryAnalyzer implements QueryHandler{

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing travel telemetry... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        PlayerSession.GalacticCoordinates galacticCoordinates = playerSession.getGalacticCoordinates();

        if(galacticCoordinates == null){
            LocationDto currentLocation = playerSession.getCurrentLocation();
            galacticCoordinates = new PlayerSession.GalacticCoordinates(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());
        }

        if (galacticCoordinates.x() == 0 && galacticCoordinates.y() == 0 && galacticCoordinates.z() == 0) {
            return process("Local Coordinates are not available.");
        }

        String instruction = "Center of the bubble (Earth) is at 0 0 0. Use the coordinates provided in light years to calculate distance. If asked about amount of fleet carrier fuel needed to cover thg distance use 90 tons of fuel per 500 light year jump to calculate the amount.";
        return process(new AiDataStruct(instruction, new DataDto(galacticCoordinates)), originalUserInput);
    }

    record DataDto(PlayerSession.GalacticCoordinates galacticCoordinates) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

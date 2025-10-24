package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

public class AnalyzeDistanceFromTheBubble extends BaseQueryAnalyzer implements QueryHandler{

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        PlayerSession.GalacticCoordinates galacticCoordinates = playerSession.getGalacticCoordinates();

        if(galacticCoordinates == null){
            LocationDto currentLocation = playerSession.getCurrentLocation();
            galacticCoordinates = new PlayerSession.GalacticCoordinates(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());
            if (galacticCoordinates.x() == 0 && galacticCoordinates.y() == 0 && galacticCoordinates.z() == 0) {
                return process("Local Coordinates are not available.");
            }
        }

        String instruction = "Center of the bubble (Earth) is at 0 0 0. Use the coordinates provided in light years to calculate distance. If asked about amount of fleet carrier fuel needed to cover thg distance use 90 tons of fuel per 500 light year jump to calculate the amount.";
        return process(new DataDto(instruction, galacticCoordinates), originalUserInput);
    }

    record DataDto(String instructions, PlayerSession.GalacticCoordinates galacticCoordinates) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

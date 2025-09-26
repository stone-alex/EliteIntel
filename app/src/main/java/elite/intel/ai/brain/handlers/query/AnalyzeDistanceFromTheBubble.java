package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeDistanceFromTheBubble extends BaseQueryAnalyzer implements QueryHandler{

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto position = playerSession.getCurrentLocation();

        String instruction = "Center of the bubble (Earth) is at 0 0 0. Use the coordinates provided in light years to calculate distance";
        return analyzeData(new DataDto(position.getX(),position.getY(), position.getZ(),instruction).toJson(), originalUserInput);
    }

    record DataDto(double x, double y, double z, String instruction) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class AnalyzeCurrentPlanetHandler extends BaseQueryAnalyzer implements QueryHandler {



    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        List<BioSampleDto> bioSamples = playerSession.getBioSamples();
        return analyzeData(new DataDto(currentLocation,bioSamples).toJson(), originalUserInput);
    }

    record DataDto(LocationDto location, List<BioSampleDto> bioSamples) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Map;

public class AnalyzeSignalDataHandler  extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        Map<Integer, FSSBodySignalsEvent> fssBodySignals = playerSession.getFssBodySignals();
        Map<String, StellarObjectDto> stellarObjects = playerSession.getStellarObjects();
        SystemBodiesDto edsmData = EdsmApiClient.searchSystemBodies(currentLocation.getStarName());

        return analyzeData(new DataDto(fssBodySignals, stellarObjects, edsmData).toJson(), originalUserInput);
    }

    record DataDto(Map<Integer, FSSBodySignalsEvent> fssBodySignals, Map<String, StellarObjectDto> stellarObjects, SystemBodiesDto edsmData ) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

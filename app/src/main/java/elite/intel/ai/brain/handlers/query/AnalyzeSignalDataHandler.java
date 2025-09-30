package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;
import java.util.Map;

public class AnalyzeSignalDataHandler  extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        Map<Integer, FSSBodySignalsEvent> fssBodySignals = playerSession.getFssBodySignals();
        Map<Long, LocationDto> stellarObjects = playerSession.getStellarObjects();
        List<BioSampleDto> completedBioSamples = playerSession.getBioCompletedSamples();
        SystemBodiesDto edsmData = EdsmApiClient.searchSystemBodies(currentLocation.getStarName());

        return analyzeData(new DataDto(currentLocation, fssBodySignals, stellarObjects, completedBioSamples, edsmData).toJson(), originalUserInput);
    }

    record DataDto(
            LocationDto currentLocation,
            Map<Integer, FSSBodySignalsEvent> fullSpectrumSignals,
            Map<Long, LocationDto> allStellarObjectsInStarSystem,
            List<BioSampleDto> completedBioScans,
            SystemBodiesDto edsmData
    ) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

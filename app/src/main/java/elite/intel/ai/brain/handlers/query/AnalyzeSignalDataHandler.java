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
        List<FSSBodySignalsEvent.Signal> fssBodySignals = playerSession.getCurrentLocation().getFssSignals();
        Map<Long, LocationDto> stellarObjects = playerSession.getLocations();
        List<BioSampleDto> completedBioSamples = playerSession.getBioCompletedSamples();
        SystemBodiesDto edsmData = EdsmApiClient.searchSystemBodies(currentLocation.getStarName());

        String instructions= "If asked about biology or life forms look at stellarObjects for detectedSignals of signalType 'Biological', also check fssBodySignals and bio samples in completedBioSamples. Return short planet name(s)";
        return analyzeData(new DataDto(currentLocation, fssBodySignals, stellarObjects, completedBioSamples, edsmData, instructions).toJson(), originalUserInput);
    }

    record DataDto(
            LocationDto currentLocation,
            List<FSSBodySignalsEvent.Signal> fssBodySignals,
            Map<Long, LocationDto> allStellarObjectsInStarSystem,
            List<BioSampleDto> completedBioScans,
            SystemBodiesDto edsmData,
            String instructions
    ) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

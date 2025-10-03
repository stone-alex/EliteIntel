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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeSignalDataHandler  extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        LocationDto currentLocation = playerSession.getCurrentLocation();
        List<FSSBodySignalsEvent.Signal> fssBodySignals = playerSession.getCurrentLocation().getFssSignals();
        Map<Long, LocationDto> allLocations = playerSession.getLocations();
        List<BioSampleDto> allComplletedBioScans = playerSession.getBioCompletedSamples();
        SystemBodiesDto edsmData = EdsmApiClient.searchSystemBodies(currentLocation.getStarName());
        Map<String, Integer> planetsRequireBioScans = planetsWithBioFormsNotYetScanned();
        Map<String, Integer> planetsWithGeoSignals = planetsWithGeoSignals();

        return analyzeData(new DataDto(fssBodySignals, allLocations, allComplletedBioScans, planetsRequireBioScans, planetsWithGeoSignals, edsmData).toJson(), originalUserInput);
    }

    record DataDto(
            List<FSSBodySignalsEvent.Signal> filteredSpectrumScans,
            Map<Long, LocationDto> allStellarObjectsInStarSystem,
            List<BioSampleDto> allComplletedBioScans,
            Map<String, Integer> planetsRequireBioScans,
            Map<String, Integer> planetsWithGeoSignals,
            SystemBodiesDto edsmData
    ) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    private Map<String, Integer> planetsWithGeoSignals() {
        Map<String, Integer> result = new HashMap<>();
        Map<Long, LocationDto> locations = playerSession.getLocations();
        for(LocationDto location : locations.values()){
            if(location.getGeoSignals() > 0){
                result.put(location.getPlanetName(), location.getGeoSignals());
            }
        }
        return result;
    }


    private Map<String, Integer> planetsWithBioFormsNotYetScanned() {
        Map<String, Integer> result = new HashMap<>();
        Map<Long, LocationDto> locations = playerSession.getLocations();

        for(LocationDto location : locations.values()){
            if(location.getBioSignals() > 0){
                int numCompletedSamples = getCompletedSamples(location.getPlanetName());
                if(location.getBioSignals() != numCompletedSamples){
                    result.put(location.getPlanetName(), location.getBioSignals() - numCompletedSamples);
                }
            }
        }
        return result;
    }

    private int getCompletedSamples(String planetName) {
        List<BioSampleDto> completedSamples = playerSession.getBioCompletedSamples();
        int result = 0;
        for(BioSampleDto sample : completedSamples){
            if(sample.getPlanetName().equalsIgnoreCase(planetName)){
                result++;
            }
        }
        return result;
    }
}

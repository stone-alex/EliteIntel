package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_SEARCH_SIGNAL_DATA;

public class AnalyzeSignalDataHandler  extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        LocationDto currentLocation = playerSession.getCurrentLocation();
        List<FSSBodySignalsEvent.Signal> fssBodySignals = playerSession.getCurrentLocation().getFssSignals();
        Map<Long, LocationDto> allLocations = playerSession.getLocations();
        List<BioSampleDto> allCompletedBioScans = playerSession.getBioCompletedSamples();
        SystemBodiesDto edsmData = EdsmApiClient.searchSystemBodies(currentLocation.getStarName());
        Map<String, Integer> planetsRequireBioScans = planetsWithBioFormsNotYetScanned();
        Map<String, Integer> planetsWithGeoSignals = planetsWithGeoSignals();

        return process(new DataDto(QUERY_SEARCH_SIGNAL_DATA.getInstructions(), fssBodySignals, allLocations, allCompletedBioScans, planetsRequireBioScans, planetsWithGeoSignals, edsmData), originalUserInput);
    }

    record DataDto(
            String instructions,
            List<FSSBodySignalsEvent.Signal> filteredSpectrumScans,
            Map<Long, LocationDto> allStellarObjectsInStarSystem,
            List<BioSampleDto> allCompletedBioScans,
            Map<String, Integer> planetsRequireBioScans,
            Map<String, Integer> planetsWithGeoSignals,
            SystemBodiesDto edsmData
    ) implements AiData {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
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

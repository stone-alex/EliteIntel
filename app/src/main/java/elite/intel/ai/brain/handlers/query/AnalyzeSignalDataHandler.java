package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiData;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.StationsDto;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

import java.util.*;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_SEARCH_SIGNAL_DATA;

public class AnalyzeSignalDataHandler  extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing... stand by..."));

        Set<FssSignalDto> detectedSignals = getDetectedSignals();
        List<FSSBodySignalsEvent.Signal> fssBodySignals = playerSession.getCurrentLocation().getFssSignals();
        Map<Long, LocationDto> allLocations = playerSession.getLocations();
        List<BioSampleDto> allCompletedBioScans = playerSession.getBioCompletedSamples();
        Map<String, Integer> planetsRequireBioScans = planetsWithBioFormsNotYetScanned();
        Map<String, Integer> planetsWithGeoSignals = planetsWithGeoSignals();

        String primaryStarName = playerSession.getPrimarySystem().getStarName();
        SystemBodiesDto edsmData = EdsmApiClient.searchSystemBodies(primaryStarName);
        DeathsDto deathsDto = EdsmApiClient.searchDeaths(primaryStarName);
        TrafficDto trafficDto = EdsmApiClient.searchTraffic(primaryStarName);
        StationsDto stationsDto = EdsmApiClient.searchStations(primaryStarName);

        return process(new DataDto(QUERY_SEARCH_SIGNAL_DATA.getInstructions(), detectedSignals, fssBodySignals, allLocations, allCompletedBioScans, planetsRequireBioScans, planetsWithGeoSignals, edsmData, deathsDto, trafficDto, stationsDto), originalUserInput);
    }

    private Set<FssSignalDto> getDetectedSignals() {
        Map<Long, LocationDto> locations = playerSession.getLocations();
        if(locations.isEmpty()) return new HashSet<>();
        for(LocationDto location : locations.values()){
            if(LocationDto.LocationType.PRIMARY_STAR.equals(location.getLocationType())){
                return location.getDetectedSignals();
            }
        }
        return new HashSet<>();
    }

    record DataDto(
            String instructions,
            Set<FssSignalDto> detectedSignals,
            List<FSSBodySignalsEvent.Signal> filteredSpectrumScans,
            Map<Long, LocationDto> allStellarObjectsInStarSystem,
            List<BioSampleDto> allCompletedBioScans,
            Map<String, Integer> planetsRequireBioScans,
            Map<String, Integer> planetsWithGeoSignals,
            SystemBodiesDto edsmData,
            DeathsDto edsmDeathData,
            TrafficDto edsmTrafficData,
            StationsDto edsmStationsData
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

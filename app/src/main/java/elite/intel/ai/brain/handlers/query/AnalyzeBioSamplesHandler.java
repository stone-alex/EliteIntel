package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.GenusDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static elite.intel.util.ExoBio.completedScansForPlanet;

public class
AnalyzeBioSamplesHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (currentLocation.getBodyId() < 0) {
            return analyzeData("Current location data is not available", originalUserInput);
        }
        Map<Long, LocationDto> locations = playerSession.getLocations();

        List<BioSampleDto> partialScans = currentLocation.getPartialBioSamples();
        List<GenusDto> genus = currentLocation.getGenus();
        List<BioSampleDto> samplesCompletedForThisPlanet = completedScansForPlanet(playerSession);
        List<BioSampleDto> allBioSamplesForThisStarSystem = playerSession.getBioCompletedSamples();
        List<String> planetNamesWithBioFormsWeHaveNotScanned = getPlanetsWithBioFormNotYetScanned(allBioSamplesForThisStarSystem, locations);
        List<String> planetNamesWithPartialBioScans = getPlanetsWithPartialBioScans(partialScans, locations);

        String instructions = "'partialScans' contains partial bio scans (3 scans require to complete a bio sample. 'genus' all genus present on current planet. 'allBioSamplesForThisStarSystem' all completed bio samples for this star system. 'planetNamesWithBioFormsWeHaveNotScanned' list of planets that contain bio forms which remain to be scanned. 'planetNamesWithPartialBioScans' list of planets that contain partial bio scans. 'planetNamesWithPartialBioScans' list of planet names where bio scans are incomplete / partial samples. Name the genus and short planet names that still require scans.";
        return analyzeData(
                new DataDto(partialScans,
                        genus,
                        samplesCompletedForThisPlanet,
                        allBioSamplesForThisStarSystem,
                        planetNamesWithBioFormsWeHaveNotScanned,
                        planetNamesWithPartialBioScans,
                        instructions
                ).toJson(), originalUserInput
        );
    }

    private List<String> getPlanetsWithPartialBioScans(List<BioSampleDto> partialScans, Map<Long, LocationDto> locations) {
        ArrayList<String> result = new ArrayList<>();
        for (LocationDto locationDto : locations.values()) {
            for (BioSampleDto sample : partialScans) {
                if (sample.getPlanetName().equalsIgnoreCase(locationDto.getPlanetName())) {
                    result.add(locationDto.getPlanetShortName());
                }
            }
        }
        return result;
    }

    private List<String> getPlanetsWithBioFormNotYetScanned(List<BioSampleDto> allBioSamplesForThisStarSystem, Map<Long, LocationDto> locations) {
        List<String> result = new ArrayList<>();
        for (LocationDto location : locations.values()) {
            for (BioSampleDto sample : allBioSamplesForThisStarSystem) {
                if (sample.getPlanetName().equalsIgnoreCase(location.getPlanetName())) {
                    result.add(location.getPlanetShortName());
                }
            }
        }
        return result;
    }

    record DataDto(List<BioSampleDto> partialBioFormScans,
                   List<GenusDto> allBioFormsOnPlanet,
                   List<BioSampleDto> bioSamplesCompletedForThisPlanet,
                   List<BioSampleDto> allBioSamplesForThisStarSystem,
                   List<String> planetNamesWithBioFormsWeHaveNotScanned,
                   List<String> planetNamesWithPartialBioScans,
                   String instructions

    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

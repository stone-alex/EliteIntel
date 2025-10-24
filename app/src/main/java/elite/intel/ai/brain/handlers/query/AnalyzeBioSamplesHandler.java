package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.GenusDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

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
            return process("Current location data is not available");
        }
        Map<Long, LocationDto> locations = playerSession.getLocations();

        List<BioSampleDto> partialScans = currentLocation.getPartialBioSamples();
        List<GenusDto> genusListForCurrentLocation = currentLocation.getGenus();
        List<BioSampleDto> samplesCompletedForThisPlanet = completedScansForPlanet(playerSession);
        List<BioSampleDto> allBioSamplesForThisStarSystem = playerSession.getBioCompletedSamples();
        List<GenusDto> genusListNotScannedForCurrentLocation = calculateGenusNotYetScanned(samplesCompletedForThisPlanet, genusListForCurrentLocation);
        List<String> planetNamesWithBioFormsWeHaveNotScanned = getPlanetsWithBioFormNotYetScanned(allBioSamplesForThisStarSystem, locations);
        List<String> planetNamesWithPartialBioScans = getPlanetsWithPartialBioScans(partialScans, locations);

        String instructions = "Analyze Elite Dangerous bio samples. 'partialScans': partial bio scans (3 scans needed per sample). 'genusListForCurrentLocation': all genus on current planet. 'allBioSamplesForThisStarSystem': completed bio samples in star system, not yet delivered to Vista Genomics. 'planetNamesWithBioFormsWeHaveNotScanned': planets with unscanned bio forms. 'planetNamesWithPartialBioScans': planets with incomplete scans. 'genusListNotScannedForCurrentLocation': unscanned genus on current planet. For queries about unscanned genus or planets, list names from 'planetNamesWithBioFormsWeHaveNotScanned' and 'genusListNotScannedForCurrentLocation'. ";

        return process(
                new DataDto(
                        instructions,
                        partialScans,
                        genusListForCurrentLocation,
                        samplesCompletedForThisPlanet,
                        allBioSamplesForThisStarSystem,
                        planetNamesWithBioFormsWeHaveNotScanned,
                        planetNamesWithPartialBioScans,
                        genusListNotScannedForCurrentLocation

                ), originalUserInput
        );
    }

    private List<GenusDto> calculateGenusNotYetScanned(List<BioSampleDto> completedSamples, List<GenusDto> genusListForCurrentLocation) {
        ArrayList<GenusDto> result = new ArrayList<>();
        for (GenusDto genus : genusListForCurrentLocation) {
            boolean found = false;
            for (BioSampleDto sample : completedSamples) {
                if (sample.getGenus().equals(genus.getSpecies())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(genus);
            }
        }
        return result;
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

    record DataDto(String instructions,
                   List<BioSampleDto> partialBioFormScans,
                   List<GenusDto> allBioFormsOnPlanet,
                   List<BioSampleDto> bioSamplesCompletedForThisPlanet,
                   List<BioSampleDto> allBioSamplesForThisStarSystem,
                   List<String> planetNamesWithBioFormsWeHaveNotScanned,
                   List<String> planetNamesWithPartialBioScans,
                   List<GenusDto> genusListNotScannedForCurrentLocation

    ) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

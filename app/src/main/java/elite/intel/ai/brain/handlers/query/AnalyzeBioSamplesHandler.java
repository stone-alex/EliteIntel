package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.GenusDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.*;

import static elite.intel.util.ExoBio.completedScansForPlanet;

public class AnalyzeBioSamplesHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing exobiology data... stand by..."));

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if (currentLocation.getBodyId() < 0) {
            return process("Current location data is not available");
        }
        Map<Long, LocationDto> locations = playerSession.getLocations();

        List<BioSampleDto> partialScans = currentLocation.getPartialBioSamples();
        List<GenusDto> genusListForCurrentLocation = currentLocation.getGenus();
        List<BioSampleDto> samplesCompletedForThisPlanet = completedScansForPlanet(playerSession);
        List<GenusDto> genusListNotScannedForCurrentLocation = calculateGenusNotYetScanned(samplesCompletedForThisPlanet, genusListForCurrentLocation);

        String instructions = "Analyze bio samples. 'partialScans': partial bio scans (3 scans needed per sample). 'genusListForCurrentLocation': all genus on current planet. 'genusListNotScannedForCurrentLocation': unscanned genus on current planet. For queries about unscanned genus list variant and species from 'genusListNotScannedForCurrentLocation'. ";

        AiDataStruct struct = new AiDataStruct(instructions, new DataDto(
                partialScans,
                genusListForCurrentLocation,
                samplesCompletedForThisPlanet,
                genusListNotScannedForCurrentLocation
        ));

        return process(struct, originalUserInput);
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

    record DataDto(
                   List<BioSampleDto> partialBioFormScans,
                   List<GenusDto> allBioFormsOnPlanet,
                   List<BioSampleDto> bioSamplesCompletedForThisPlanet,
                   List<GenusDto> genusListNotScannedForCurrentLocation

    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

    }
}

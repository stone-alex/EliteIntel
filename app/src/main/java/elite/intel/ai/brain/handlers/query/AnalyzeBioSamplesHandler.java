package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.GenusDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

import static elite.intel.util.ExoBio.completedScansForPlanet;

public class
AnalyzeBioSamplesHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if(currentLocation == null) {return analyzeData("Current location data is not available", originalUserInput);}

        List<BioSampleDto> partialScans = currentLocation.getPartialBioSamples();
        List<GenusDto> genus = currentLocation.getGenus();
        List<BioSampleDto> samplesCompletedForThisPlanet = completedScansForPlanet(playerSession);
        List<BioSampleDto> allBioSamplesForThisStarSystem = playerSession.getBioCompletedSamples();

        String instructions = "Use Elite Dangerous terminology and be concise. For exobiology queries, return only essential information. For a specific planet, return data only for that planet, excluding star system names. For queries about biological signals (e.g., 'Biofilm to Heaven'), interpret 'scanned' as detected biological signals (from locations.genus or locations.bioSignals) that lack a completed sample scan in bio_samples (where bioSampleCompleted is false or no entry exists). Return only the short names of planets with un-scanned signals matching the queried species or genus, e.g., 'ABC 4'. If no such planets exist, return 'None'. Exclude planets where all bio signals are fully sampled (bioSampleCompleted: true in bio_samples). For non-exobiology queries (e.g., geological, non-human signals, hazardous resources), provide brief, relevant data specific to the query. Avoid extraneous details.";
        return analyzeData(new DataDto(partialScans, genus, samplesCompletedForThisPlanet, allBioSamplesForThisStarSystem, instructions).toJson(), originalUserInput);
    }


    record DataDto(List<BioSampleDto> partialScans,
                   List<GenusDto> allGenusOnPlanet,
                   List<BioSampleDto> samplesCompletedForThisPlanet,
                   List<BioSampleDto> allBioSamplesForThisStarSystem,
                   String instructions

    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

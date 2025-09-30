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

public class AnalyzeBioSamplesHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if(currentLocation == null) {return analyzeData("Current location data is not available", originalUserInput);}

        List<BioSampleDto> partialScans = currentLocation.getPartialBioSamples();
        List<GenusDto> genus = currentLocation.getGenus();
        List<BioSampleDto> samplesCompletedForThisPlanet = completedScansForPlanet(playerSession);
        List<BioSampleDto> allBioSamplesForThisStarSystem = playerSession.getBioCompletedSamples();

        return analyzeData(new DataDto(partialScans, genus, samplesCompletedForThisPlanet, allBioSamplesForThisStarSystem).toJson(), originalUserInput);
    }


    record DataDto(List<BioSampleDto> partialScans,
                   List<GenusDto> allGenusOnPlanet,
                   List<BioSampleDto> samplesCompletedForThisPlanet,
                   List<BioSampleDto> allBioSamplesForThisStarSystem
    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

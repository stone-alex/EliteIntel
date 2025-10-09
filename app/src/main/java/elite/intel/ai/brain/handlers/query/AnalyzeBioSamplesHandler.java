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

        String instructions = "partialBioFormScans are partial bio samples for currentLocation. allBioFormsOnPlanet are all exobiology on the planet. allBioSamplesForThisStarSystem are all exobiology for the current star system. Co-relate this data using planetName and genus for exobiology. For planet names return planetShortName. For exobiology Return genus. The bodyId is only unique within star system, but not across all star systems. Do not use it.";
        return analyzeData(new DataDto(partialScans, genus, samplesCompletedForThisPlanet, allBioSamplesForThisStarSystem, instructions).toJson(), originalUserInput);
    }


    record DataDto(List<BioSampleDto> partialBioFormScans,
                   List<GenusDto> allBioFormsOnPlanet,
                   List<BioSampleDto> bioSamplesCompletedForThisPlanet,
                   List<BioSampleDto> allBioSamplesForThisStarSystem,
                   String instructions

    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

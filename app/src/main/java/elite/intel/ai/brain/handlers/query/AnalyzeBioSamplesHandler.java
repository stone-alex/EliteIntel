package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class AnalyzeBioSamplesHandler extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if(currentLocation == null) {return analyzeData("Current location data is not available", originalUserInput);}

        List<BioSampleDto> partialScans = currentLocation.getBioScans();
        List<SAASignalsFoundEvent.Genus> genus = currentLocation.getGenus();
        List<BioSampleDto> completedBioSamples = playerSession.getBioSamples();

        return analyzeData(new DataDto(partialScans, genus, completedBioSamples).toJson(), originalUserInput);
    }

    record DataDto(List<BioSampleDto> partialScans, List<SAASignalsFoundEvent.Genus> allGenusOnPlanet, List<BioSampleDto> completedSamples) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

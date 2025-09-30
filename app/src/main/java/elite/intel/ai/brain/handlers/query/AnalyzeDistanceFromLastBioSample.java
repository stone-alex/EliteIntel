package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeDistanceFromLastBioSample extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Status status = Status.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        if(currentLocation == null) {
            return analyzeData(toJson("Current location is not available."), originalUserInput);
        }
        if (status.getStatus() == null) {
            return analyzeData(toJson("No data available"), originalUserInput);
        }

        double latitude = status.getStatus().getLatitude();
        double longitude = status.getStatus().getLongitude();
        double planetRadius = status.getStatus().getPlanetRadius();

        if (latitude == 0 || longitude == 0 || planetRadius == 0) {
            return analyzeData(toJson("Your current position is not available."), originalUserInput);
        }

        if(currentLocation.getPartialBioSamples().isEmpty()){
            return analyzeData(toJson("No bio samples available."), originalUserInput);
        }

        BioSampleDto bioSample = currentLocation.getPartialBioSamples().getLast();
        return analyzeData(new DataDto(latitude, longitude, planetRadius, bioSample).toJson(), originalUserInput);
    }

    record DataDto(double userLatitude, double userLongitude, double planetRadius, BioSampleDto bioSample) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

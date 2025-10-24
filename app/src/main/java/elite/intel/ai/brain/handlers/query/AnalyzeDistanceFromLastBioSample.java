package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

public class AnalyzeDistanceFromLastBioSample extends BaseQueryAnalyzer implements QueryHandler {


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Status status = Status.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();

        String instructions = "use userLatitude, userLongitude, bioSample.scanLatitude, bioSample.scanLongitude and planetRadius to calculate distance to the last partial bio-sample.";

        if (status.getStatus() == null) {
            return process("No planet data available");
        }

        double latitude = status.getStatus().getLatitude();
        double longitude = status.getStatus().getLongitude();
        double planetRadius = status.getStatus().getPlanetRadius();

        if (latitude == 0 || longitude == 0 || planetRadius == 0) {
            return process("Your current position is not available.");
        }

        if(currentLocation.getPartialBioSamples().isEmpty()){
            return process("No partial bio scans data.");
        }

        BioSampleDto bioSample = currentLocation.getPartialBioSamples().getLast();
        return process(new DataDto(instructions, latitude, longitude, planetRadius, bioSample), originalUserInput);
    }

    record DataDto(String instructions, double userLatitude, double userLongitude, double planetRadius, BioSampleDto bioSample) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

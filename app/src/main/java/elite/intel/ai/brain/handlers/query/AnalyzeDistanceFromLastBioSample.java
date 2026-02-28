package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.NavigationUtils;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzeDistanceFromLastBioSample extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing exobiology collection data. Stand by."));

        Status status = Status.getInstance();
        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());

        String instructions = """
                use userLatitude, userLongitude, bioSample.scanLatitude, bioSample.scanLongitude and planetRadius to calculate distance to the last partial bio-sample.
                """;

        if (status.getStatus() == null) {
            return process("No planet data available");
        }

        double latitude = status.getStatus().getLatitude();
        double longitude = status.getStatus().getLongitude();
        double planetRadius = status.getStatus().getPlanetRadius();

        if (latitude == 0 || longitude == 0 || planetRadius == 0) {
            return process("Your current position is not available.");
        }

        if (currentLocation.getPartialBioSamples().isEmpty()) {
            return process("No partial bio scans data.");
        }

        BioSampleDto bioSample = currentLocation.getPartialBioSamples().getLast();
        double distance = NavigationUtils.calculateSurfaceDistance(
                bioSample.getScanLatitude(),
                bioSample.getScanLongitude(),
                status.getStatus().getLatitude(),
                status.getStatus().getLongitude(),
                planetRadius,
                status.getStatus().getAltitude()
        );


        return process(new AiDataStruct(instructions, new DataDto((int) distance, bioSample.getGenus())), originalUserInput);
    }

    record DataDto(int distanceInMeters, String genusName) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

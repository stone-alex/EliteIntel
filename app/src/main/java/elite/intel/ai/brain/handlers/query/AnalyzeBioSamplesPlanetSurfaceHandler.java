package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.GenusDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.ExoBio;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.util.ExoBio.completedScansForPlanet;

public class AnalyzeBioSamplesPlanetSurfaceHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing exobiology data... Stand by..."));


        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        if (currentLocation.getBodyId() < 0) {
            return process("Current location data is not available");
        }
        List<BioSampleDto> partialScans = currentLocation.getPartialBioSamples();
        List<GenusDto> genusListForCurrentLocation = currentLocation.getGenus();
        List<ExoBio.DataDto> completedScansForPlanet = completedScansForPlanet(playerSession.getBioCompletedSamples(), currentLocation.getPlanetName());
        List<GenusDto> genusListNotScannedForCurrentLocation = calculateGenusNotYetScanned(completedScansForPlanet, genusListForCurrentLocation);

        String instructions = """
                Answer user question about genus/organics presented in data set.
                Data set contains:
                    - partialBioFormScans contains list of partially scanned genus/organics. (3 scans require to complete a sample).
                    - allBioFormsOnPlanet contains list genus/organics of all organic bio forms present.
                    - completedScansForPlanet contains list of genus/organics that still require scan.
                    = genusListNotScannedForCurrentLocation contains list of genus/organics we still have to scan.
                    return genus names matching user question.
                """;

        AiDataStruct struct = new AiDataStruct(instructions, new DataDto(
                partialScans,
                genusListForCurrentLocation,
                completedScansForPlanet,
                genusListNotScannedForCurrentLocation
        ));

        return process(struct, originalUserInput);
    }

    private List<GenusDto> calculateGenusNotYetScanned(List<ExoBio.DataDto> completedSamples, List<GenusDto> genusListForCurrentLocation) {
        ArrayList<GenusDto> result = new ArrayList<>();
        for (GenusDto genus : genusListForCurrentLocation) {
            boolean found = false;
            for (ExoBio.DataDto sample : completedSamples) {
                if (sample.genus().equals(genus.getSpecies())) {
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
            List<ExoBio.DataDto> completedScansForPlanet,
            List<GenusDto> genusListNotScannedForCurrentLocation

    ) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

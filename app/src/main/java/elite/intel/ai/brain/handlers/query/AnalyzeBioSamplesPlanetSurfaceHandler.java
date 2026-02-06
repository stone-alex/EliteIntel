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
        List<ExoBio.DataDto> completedScansForThisLocation = completedScansForPlanet(playerSession.getBioCompletedSamples(), currentLocation.getPlanetName());
        List<GenusDto> genusListNotScannedForCurrentLocation = calculateGenusNotYetScanned(completedScansForThisLocation, genusListForCurrentLocation);

        String instructions = """
                Answer user questions about remaining genus/organics to scan on the current planet.
                
                Available data:
                - allBioFormsOnPlanet: list of ALL genus/species known to exist on the planet (with rewards)
                - completedScansForPlanet: list of FULLY completed scans (look for completed: true and scanXof3: 3)
                - partialBioFormScans: list of partial/in-progress scans (usually 1 or 2 scans done)
                - genusListNotScannedForCurrentLocation: explicit list of genus that still need at least one scan (most authoritative when present)
                
                Rules (in priority order):
                1. If genusListNotScannedForCurrentLocation exists and is non-empty → those are the only remaining genus to scan. Return only those genus names that match the user's question.
                2. If genusListNotScannedForCurrentLocation is missing or empty → check completedScansForPlanet:
                   - A genus is DONE if it has an entry with completed: true (and preferably scanXof3: 3)
                   - Remaining genus = genera in allBioFormsOnPlanet that do NOT appear in completedScansForPlanet with completed: true
                3. Never report a genus as remaining if it has a completed: true entry, even if it appears in allBioFormsOnPlanet or partial scans.
                4. If no remaining genus after above checks → clearly state "All known organic samples on this planet are already completed."
                
                Return only the matching genus names (or none/"all done") — be concise.
                """;

        AiDataStruct struct = new AiDataStruct(instructions, new DataDto(
                partialScans,
                genusListForCurrentLocation,
                completedScansForThisLocation,
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

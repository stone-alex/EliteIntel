package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnalyzeBioSignalsStarSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing bio data for star system."));
        List<BioSampleDto> allCompletedBioScans = playerSession.getBioCompletedSamples();
        List<PlanetsToScan> planetsRequireBioScans = planetsWithBioFormsNotYetScanned();

        String instructions = """
                You are a strict data-only responder for this query.
                Follow these rules in exact order:
                
                1. Look ONLY at the two fields in the provided JSON:
                   - allCompletedBioScans (array): shows bodies where bio samples are already fully scanned
                   - planetsRequireBioScans (object): shows bodies that STILL NEED bio scans (key = planet name, value = number of remaining scans needed)
                
                2. Decision logic — apply exactly:
                   - If planetsRequireBioScans has any entry with value ≥ 1 → Answer YES, there are organics still needing scans. Name each planet and the remaining number.
                   - Else if allCompletedBioScans is non-empty but planetsRequireBioScans is empty → Answer NO active unscanned organics remain (only completed scans exist).
                   - Else (both empty) → Answer NO biological/organic signals detected in this system.
                
                3. Keep response_text extremely short (1 sentence max), factual, no enthusiasm, no extra commentary.
                
                Example of good short answers (for reference only — do NOT copy these literally):
                - "Yes, planet 'ABC 1 b' still needs 2 bio scans. (name genus if available)"
                - "No organics left to scan — all detected samples completed."
                - "No biological signals detected."       
                """;

        return process(new AiDataStruct(instructions, new DataDto(allCompletedBioScans, planetsRequireBioScans)), originalUserInput);
    }

    private List<PlanetsToScan> planetsWithBioFormsNotYetScanned() {
        List<PlanetsToScan> result = new ArrayList<>();
        Collection<LocationDto> locations = locationManager.findAllBySystemAddress(playerSession.getLocationData().getSystemAddress());

        for (LocationDto location : locations) {
            int numCompletedSamples = getCompletedSamples(location.getPlanetName());
            List<FSSBodySignalsEvent.Signal> fssSignals = location.getFssSignals();
            int bioSignalCounter = 0;
            if (fssSignals != null && !fssSignals.isEmpty()) {
                for (FSSBodySignalsEvent.Signal signal : fssSignals) {
                    if (signal.getTypeLocalised().toLowerCase().contains("bio")) {
                        bioSignalCounter = bioSignalCounter + signal.getCount();
                    }
                }
            }
            if (bioSignalCounter > 0) {
                result.add(new PlanetsToScan(location.getPlanetName(), bioSignalCounter - numCompletedSamples));
            } else if (location.getBioSignals() != numCompletedSamples) {
                result.add(new PlanetsToScan(location.getPlanetName(), location.getBioSignals() - numCompletedSamples));
            }

        }
        return result;
    }

    private int getCompletedSamples(String planetName) {
        List<BioSampleDto> completedSamples = playerSession.getBioCompletedSamples();
        int result = 0;
        for (BioSampleDto sample : completedSamples) {
            if (sample.getPlanetName().equalsIgnoreCase(planetName)) {
                result++;
            }
        }
        return result;
    }

    record DataDto(List<BioSampleDto> allCompletedBioScans, List<PlanetsToScan> planetsRequireBioScans) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    record PlanetsToScan(String planetName, int remainingOrganicsToScan) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

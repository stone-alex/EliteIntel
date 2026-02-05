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
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static elite.intel.util.StringUtls.getIntSafely;

public class AnalyzeBioSignalsStarSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing bio data for star system."));
        List<BioSampleDto> allCompletedBioScans = playerSession.getBioCompletedSamples();
        List<PlanetsToScan> planetsRequireBioScans = planetsWithBioFormsNotYetScanned();

        String instructions = """
                You are a strict data-only responder for this query.
                Answer only what is user asking using this data. Do not read back the entire data set.
                   - planetsRequireBioScans (object): shows bodies that STILL NEED bio scans (key = planet name, value = number of remaining scans needed).
                   - allCompletedBioScans (array): shows bodies where bio samples are already fully scanned
                """;

        return process(new AiDataStruct(instructions, new DataDto(planetsRequireBioScans, toBioSameplDataList(allCompletedBioScans))), originalUserInput);
    }

    private List<BioSampleData> toBioSameplDataList(List<BioSampleDto> allCompletedBioScans) {
        LinkedList<BioSampleData> result = new LinkedList<>();
        for (BioSampleDto data : allCompletedBioScans) {
            Integer numScans = data.getScanXof3();
            if (numScans != null && numScans < 3) {
                result.add(new BioSampleData(data.getPlanetShortName(), data.getGenus(), data.getSpecies(), 3 - numScans));
            }
        }
        return result;
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
                result.add(new PlanetsToScan(location.getPlanetShortName(), bioSignalCounter - numCompletedSamples));
            } else if (location.getBioSignals() != numCompletedSamples) {
                result.add(new PlanetsToScan(location.getPlanetShortName(), location.getBioSignals() - numCompletedSamples));
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

    record DataDto(List<PlanetsToScan> planetsRequireBioScans, List<BioSampleData> allCompletedBioScans) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    record BioSampleData(String planetShortName, String genus, String species, Integer samplesRequired) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    record PlanetsToScan(String planetName, int remainingOrganicsToScan) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

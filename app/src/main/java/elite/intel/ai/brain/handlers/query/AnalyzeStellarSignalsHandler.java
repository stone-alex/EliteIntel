package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.StringUtls;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.*;

public class AnalyzeStellarSignalsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {


        String instructions = """
                Answer the user's question about signals detected in this star system.
                
                Data fields:
                - stellarObjectSignals: list of signal entries per body or ring
                  - bodyName: set if this entry is for a planet or moon (null for rings)
                  - ringName: set if this entry is for a ring (null for planets)
                  - signalType: the category label for this entry
                  - hotspotsAndSignals: map of signal type to count for this body or ring
                - discoveredSignals: known points of interest (Resource Sites, Conflict Zones, etc.)
                  - locationName: body where the signal was detected
                  - signalName: name of the point of interest
                  - signalType: type classification
                
                Rules:
                - If asked about biological signals: use stellarObjectSignals where bodyName is set and hotspotsAndSignals contains a biological type. List the planet names only.
                - If asked about geological signals: use stellarObjectSignals where bodyName is set and hotspotsAndSignals contains a geological type. List the planet names only.
                - If asked about mining hotspots or rings: use stellarObjectSignals where ringName is set. List ring name and hotspot types with counts.
                - If asked about conflict zones, hunting grounds, resource sites, or points of interest: use discoveredSignals. Resource extraction sites are hunting grounds, not hotspots.
                - If asked broadly about all signals: provide a brief summary across all categories.
                - Answer only what was asked. One sentence per body or ring group.
                """;
        List<ToYamlConvertable> signals = aggregateSignals();
        List<ToYamlConvertable> discoveredSignals = toDiscoveredSignals();
        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(signals, discoveredSignals)
                ),
                originalUserInput
        );
    }


    private List<ToYamlConvertable> aggregateSignals() {
        long systemAddress = playerSession.getLocationData().getSystemAddress();
        Collection<LocationDto> locations = locationManager.findAllBySystemAddress(systemAddress);
        // body → ringId → type → count
        List<ToYamlConvertable> result = new ArrayList<>();
        Map<String, Map<String, Map<String, Integer>>> grouping = new LinkedHashMap<>();
        for (LocationDto location : locations) {
            String body = location.getPlanetShortName();
            if (body == null || body.isBlank()) {
                continue;
            }

            String ringName = location.getPlanetShortName();

            Map<String, Map<String, Integer>> bodyMap = grouping.computeIfAbsent(body, k -> new LinkedHashMap<>());
            Map<String, Integer> hotspots = bodyMap.computeIfAbsent(ringName, k -> new LinkedHashMap<>());

            // SAA signals (surface / geological / biological usually)
            List<SAASignalsFoundEvent.Signal> saa = location.getSaaSignals();
            if (saa != null) {
                for (SAASignalsFoundEvent.Signal s : saa) {
                    String type = StringUtls.humanizeBindingName(s.getType());
                    if (!type.isBlank()) {
                        hotspots.merge(type, s.getCount(), Integer::sum);
                    }
                }
            }

            // FSS signals (mostly rings + some surface)
            List<FSSBodySignalsEvent.Signal> fss = location.getFssSignals();
            if (fss != null) {
                for (FSSBodySignalsEvent.Signal s : fss) {
                    String type = s.getTypeLocalised();
                    if (type != null && !type.isBlank()) {
                        hotspots.merge(type, s.getCount(), Integer::sum);
                    }
                }
            }
        }


        for (Map.Entry<String, Map<String, Map<String, Integer>>> bodyEntry : grouping.entrySet()) {
            String body = bodyEntry.getKey();
            for (Map.Entry<String, Map<String, Integer>> entry : bodyEntry.getValue().entrySet()) {
                Map<String, Integer> signals = entry.getValue();
                if (signals.isEmpty()) {
                    continue;
                }
                // Ugly Hack to get around the inconstant data form game.
                String ringName = body.contains("Ring") ? body : null;
                String planetName = body.contains("Ring") ? null : body;
                result.add(new SignalEntry(planetName, entry.getKey(), ringName, signals));
            }
        }
        return result;
    }

    private List<ToYamlConvertable> toDiscoveredSignals() {
        List<ToYamlConvertable> discoveredSignals;
        Collection<LocationDto> locations = locationManager.findAllBySystemAddress(playerSession.getLocationData().getSystemAddress());
        List<ToYamlConvertable> list = new ArrayList<>();
        locations.forEach(location -> {
            Set<FssSignalDto> detectedSignals = location.getDetectedSignals();
            detectedSignals.stream().map(signal -> new DiscoveredSignal(
                    location.getPlanetName(),
                    signal.getSignalName(), signal.getSignalType()
            )).forEachOrdered(list::add);
        });
        discoveredSignals = list;
        return discoveredSignals;
    }

    private record DiscoveredSignal(String locationName, String signalName, String signalType) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }


    private record SignalEntry(
            String bodyName,
            String signalType,
            String ringName,
            Map<String, Integer> hotspotsAndSignals
    ) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    private record DataDto(List<ToYamlConvertable> stellarObjectSignals, List<ToYamlConvertable> discoveredSignals) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}
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
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.*;

public class AnalyzeStellarSignalsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {


        String instructions = """
                Data format:
                - "body": short name (e.g. "4", "2 d", "6z")
                - "type": "Biological", "geological signal", "ring / mining hotspot"
                - "ring": letter if ring (e.g. "A", "B")
                - "hotspots": {"Type": count, ...}
                - discoveredSignals contains hunting/battle grounds such as "Resource Sites",  "Conflict Zones" and other points of interest.
                
                Rules (priority):
                Crucial: Bio/Geo/planet Q → ignore rings, bio. Ring/mining Q → ignore planets.
                IF asked about geological signals
                1. Geo/surface/planets: ONLY Geological planets.
                   "Signals found. Geological signals on planets a, b, c and z." or "No geological signals on record."
                
                IF asked about biological signals
                2. Biological: ONLY Biological planets.
                   "signals found. Biological signals on planets a, b, c and z." or "No bio signals on record."
                
                IF asked about ring mining hotspots
                3. Rings/mining/hotspots: ONLY resource rings.
                   "Signals found. Mining hotspots in ring 2 B: Serendibite times 3, Rhodplumsite times 1. Ring 4 A: Platinum, Gold, Silver." or "No mining hotspots on record."
                
                IF asked about conflict zones, battle grounds, hunting grounds, resource sites
                    "X resource sites found" or "X conflict zones found". List these points of interest.
                    IF nothing found return "No conflict zones on record."
                
                ELSE
                5. General (user is asking broadly about signals in the system)
                   Provide board summary of detected signals.
                
                RESOURCE EXTRACTION SITES ARE NOT HOT SPOTS, THEY ARE HUNTING GROUNDS
                
                All:
                - TTS-safe: letters, numbers, spaces, commas, dots, "times"
                - 1 sentence per ring/planet group
                - Natural order, no mix
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
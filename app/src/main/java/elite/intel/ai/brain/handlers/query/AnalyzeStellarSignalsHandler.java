package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.SAASignalsFoundEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.StringUtls;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.*;

public class AnalyzeStellarSignalsHandler extends BaseQueryAnalyzer implements QueryHandler {
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final HashSet<FssSignalDto> fssSignals = new HashSet<>();
    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        List<SignalEntry> signals = aggregateSignals(playerSession.getLocations());
        String instructions = """
                Data format:
                - "body": short name (e.g. "4", "2 d", "6z")
                - "type": "Biological", "geological signal", "ring / mining hotspot"
                - "ring": letter if ring (e.g. "A", "B")
                - "hotspots": {"Type": count, ...}
                
                Rules (priority):
                Crucial: Bio/Geo/planet Q → ignore rings, bio. Ring/mining Q → ignore planets.
                IF asked about geological signals
                1. Geo/surface/planets: ONLY Geological planets.
                   "Signals found. Geological signals on planets a, b, c and z." or "No geological signals on record."
                
                IF asked about biological signals
                2. Biological: ONLY Biological planets.
                   "signals found. Biological signals on planets a, b, c and z." or "No bio signals on record."
                
                IF asked about ring mining hotspots
                3. Rings/mining/hotspots/resources: ONLY resource rings.
                   "Signals found. Mining hotspots in ring 2 B: Serendibite times 3, Rhodplumsite times 1. Ring 4 A: Platinum, Gold, Silver." or "No mineable hotspots on record."
                
                ELSE
                4. General (no type specified by user)
                   Ex: "Signals found. Geological/Biological signals on planets 2 d, 6z. Mining hotspots in ring 2 B: Serendibite times 3."
                
                RESOURCE EXTRACTION SITES ARE NOT HOT SPOTS, THEY ARE HUNTING GROUNDS
                
                All:
                - TTS-safe: letters, numbers, spaces, commas, dots, "times"
                - 1 sentence per ring/planet group
                - Natural order, no mix
                - "Yes..." if data, end period, no extras.
                """;


        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(signals, fssSignals)
                ),
                originalUserInput
        );
    }


    private List<SignalEntry> aggregateSignals(Map<Long, LocationDto> locations) {
        // body → ringId → type → count
        List<SignalEntry> result = new ArrayList<>();
        Map<String, Map<String, Map<String, Integer>>> grouping = new LinkedHashMap<>();
        for (LocationDto location : locations.values()) {
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
            fssSignals.addAll(location.getDetectedSignals());
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
                String planetName = body.contains("Ring")? null: body;
                result.add(new SignalEntry(planetName, entry.getKey(), ringName, signals));
            }
        }

        return result;
    }


    public record SignalEntry(
            String bodyName,
            String signalType,
            String ringName,
            Map<String, Integer> hotspotsAndSignals
    ) implements ToJsonConvertible {
        @Override
        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    public record DataDto(List<SignalEntry> stellarObjectSignals, Set<FssSignalDto> fssSignals) implements ToJsonConvertible {
        @Override
        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}
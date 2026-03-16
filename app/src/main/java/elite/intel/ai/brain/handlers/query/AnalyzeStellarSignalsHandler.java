package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
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
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing signals data. Stand by."));

        String instructions = """
                Answer the user's question about signals detected in this star system.
                Respond in plain spoken English only. No lists, no bullets, no numbering, no markdown.
                Two to four sentences maximum. Answer only what was asked.
                
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
                - Biological signals: name the planets that have them and the count.
                - Geological signals: name the planets that have them and the count.
                - Mining hotspots or rings: name the ring and hotspot types with counts.
                - Conflict zones, resource sites, points of interest: use discoveredSignals only.
                - Broad summary: one sentence each for bio, geo, mining, and points of interest if present.
                - Skip categories with no data. Do not say "none detected" for every category.
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
        Collection<LocationDto> locations = locationManager.findAllBySystemAddress(playerSession.getLocationData().getSystemAddress());

        // Named signals worth calling out individually (stations, megaships, notable POI)
        List<ToYamlConvertable> named = new ArrayList<>();
        // Anonymous signals (fleet carriers etc.) aggregated by type → count
        Map<String, Integer> typeCounts = new LinkedHashMap<>();

        for (LocationDto location : locations) {
            for (FssSignalDto signal : location.getDetectedSignals()) {
                String type = signal.getSignalType();
                String name = signal.getSignalName();
                boolean isAnonymous = name == null || name.isBlank()
                        || name.startsWith("$")
                        || (type != null && type.toLowerCase().contains("fleetcarrier"));
                if (isAnonymous) {
                    typeCounts.merge(type != null ? type : "Unknown", 1, Integer::sum);
                } else {
                    named.add(new DiscoveredSignal(location.getPlanetName(), name, type));
                }
            }
        }

        // Append aggregated anonymous counts as summary entries
        typeCounts.forEach((type, count) ->
                named.add(new DiscoveredSignal(null, count + "x " + type, type)));

        return named;
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
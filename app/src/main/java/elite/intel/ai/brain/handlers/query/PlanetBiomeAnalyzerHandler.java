package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public class PlanetBiomeAnalyzerHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing planetary and biome data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        String instructions = """
                You are a strict classifier. IGNORE all planet properties except planetShortName, planetClass, atmosphere, temperature, volcanism (if relevant) — but ONLY to match against genusToBiome map.
                
                ONLY source: the provided genusToBiome map.
                
                For each planet:
                - Find ALL genera whose conditions can POSSIBLY be met (lenient/partial match allowed, do NOT require 100% fit).
                - Output: "Planet <planetName>: " followed by comma-separated list (no trailing comma).
                - Number of genera listed MUST be equal or greater than numBioSignals.
                - If ZERO plausible matches but numBioSignals > 0 → list "Bacterium"
                - If no matches and numBioSignals = 0 → "Planet <shortName>: no matching genus found"
                - Pure text only. No explanations, no stats, no other words.
                
                Output format: { "type":"chat", "response_text": "Planet X: Genus1, Genus2. Planet Y: GenusA" }
                
                Example for 2 signals with good matches: Planet B 3: X, Y (wher X and Y are genus)
                """;

        Map<Long, LocationDto> locations = playerSession.getLocations();
        JsonElement key = params.get("key");
        String planetName = key == null ? null : key.getAsString().replace(" ", "");

        if (planetName != null) {
            LocationDto location = findFirstMatchingLocation(locations, planetName);
            if (location != null) {
                return process(new AiDataStruct(instructions, new DataDto1(BioForms.getGenusToBiome(), location, starSystemCharacteristics(locations.values()))), originalUserInput);
            } else {
                return process("planet " + planetName + " not found in data");
            }
        } else {
            List<LocationData> planetsWithBioSignals = findPlanetsWithBioSignals(locations);
            if (locations != null && !locations.isEmpty()) {
                return process(new AiDataStruct(instructions, new DataDto2(BioForms.getGenusToBiome(), planetsWithBioSignals, starSystemCharacteristics(locations.values()))), originalUserInput);
            } else {
                return process("planet " + planetName + " not found in data");
            }
        }
    }

    public LocationDto findFirstMatchingLocation(Map<Long, LocationDto> locations, String planetName) {
        if (planetName == null || planetName.trim().isEmpty()) {
            return null;
        }

        Optional<LocationDto> firstMatch = Optional.empty();
        for (LocationDto locationDto : locations.values()) {
            String lowerPlanetName = planetName.toLowerCase();
            String lowerShortName = trimToNull(locationDto.getPlanetShortName().toLowerCase());
            if (lowerShortName != null && lowerPlanetName.contains(lowerShortName) && planetName.length() > 0) {
                firstMatch = Optional.of(locationDto);
                break;
            }
        }

        return firstMatch.orElse(null);
    }

    public List<LocationData> findPlanetsWithBioSignals(Map<Long, LocationDto> locations) {
        List<LocationData> result = new ArrayList<>();
        for (LocationDto v : locations.values()) {
            Set<FssSignalDto> detectedSignals = v.getDetectedSignals();
            int bioSignalCounter = 0;
            for (FssSignalDto signal : detectedSignals) {
                if ("Biological".equalsIgnoreCase(signal.getSignalType())) {
                    bioSignalCounter++;
                }
            }
            if (bioSignalCounter > 0 || v.getBioSignals() > 0) {
                result.add(
                        new LocationData(
                                v.getPlanetShortName(),
                                bioSignalCounter,
                                v.getPlanetClass(),
                                String.valueOf(v.getDistance()),
                                v.getVolcanism(),
                                v.getAtmosphere(),
                                String.valueOf(v.getSurfaceTemperature())
                        )
                );
            }
        }
        return result;
    }

    private String starSystemCharacteristics(Collection<LocationDto> starSystem) {
        StringBuilder sb = new StringBuilder();
        sb.append("System:");
        for (LocationDto stellarObject : starSystem) {
            if (stellarObject.getStarClass() == null && stellarObject.getPlanetClass() != null) {
                sb.append(" has planet type:'").append(stellarObject.getPlanetClass()).append("' distance:").append(stellarObject.getDistance()).append(", ");
            } else if (stellarObject.getStarClass() != null) {
                sb.append("Star type").append(stellarObject.getBodyType()).append(" present, ");
            }
        }
        sb.append(" .");

        return sb.toString();
    }

    record LocationData(String planetName, int numBioSignals, String planetClass, String distance, String vulcanism, String atmosphere, String temperature) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    record DataDto1(Map<String, String> genusToBiome, LocationDto location, String starSystemCharacteristics) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    record DataDto2(Map<String, String> genusToBiome, Collection<LocationData> locations, String starSystemCharacteristics) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

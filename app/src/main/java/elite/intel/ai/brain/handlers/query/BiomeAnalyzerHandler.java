package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.commons.BiomeAnalyzer;
import elite.intel.ai.brain.commons.BiomeAnalyzer.LocationData;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public class BiomeAnalyzerHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing planetary and biome data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        BiomeAnalyzer biomeAnalyzer = BiomeAnalyzer.getInstance();

        JsonElement key = params.get("key");
        String planetName = key == null ? null : key.getAsString().replace(" ", "");

        if (planetName != null) {
            LocationDto firstMatchingLocation = findFirstMatchingLocation(playerSession.getLocations(), planetName);
            biomeAnalyzer.analyzeBiome(
                    originalUserInput,
                    new LocationData(
                            firstMatchingLocation.getPlanetShortName(),
                            firstMatchingLocation.getBioSignals(),
                            firstMatchingLocation.getPlanetClass(),
                            String.valueOf(firstMatchingLocation.getDistance()),
                            firstMatchingLocation.getVolcanism(),
                            firstMatchingLocation.getAtmosphere(),
                            String.valueOf(firstMatchingLocation.getSurfaceTemperature())
                    )
            );
        } else {
            biomeAnalyzer.analyzeBiome(originalUserInput, findPlanetsWithBioSignals(playerSession.getLocations()));
        }
        return null;
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

    public LocationData[] findPlanetsWithBioSignals(Map<Long, LocationDto> locations) {
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
        return result.toArray(LocationData[]::new);
    }
}

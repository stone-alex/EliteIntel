package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.commons.BiomeAnalyzer;
import elite.intel.ai.brain.commons.BiomeAnalyzer.LocationData;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public class BiomeAnalyzerHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final BiomeAnalyzer biomeAnalyzer = BiomeAnalyzer.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing planetary and biome data... Stand by..."));

        JsonElement key = params.get("key");
        String planetName = key == null ? null : key.getAsString().replace(" ", "");
        Collection<LocationDto> allStellarObjectsInStarSystem = locationManager.findAllBySystemAddress(
                playerSession.getLocationData().getSystemAddress()
        );

        if (planetName != null && !planetName.isBlank()) {
            LocationDto firstMatchingLocation = findFirstMatchingLocation(allStellarObjectsInStarSystem, planetName);
            if(firstMatchingLocation == null) return process("No match found for "+planetName);
            return biomeAnalyzer.analyzeBiome(
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
            return biomeAnalyzer.analyzeBiome(originalUserInput, findPlanetsWithBioSignals(allStellarObjectsInStarSystem));
        }
    }

    public LocationDto findFirstMatchingLocation(Collection<LocationDto> locations, String planetName) {
        if (planetName == null || planetName.trim().isEmpty()) {
            return null;
        }

        for (LocationDto locationDto : locations) {
            String lowerPlanetName = locationDto.getPlanetShortName().toLowerCase().replace("planet", "").replace(" ", "");
            if (lowerPlanetName.contains(planetName.replace(" ", "")) && !planetName.isEmpty()) {
                return locationDto;
            }
        }

        return null;
    }

    public LocationData[] findPlanetsWithBioSignals(Collection<LocationDto> locations) {
        List<LocationData> result = new ArrayList<>();
        for (LocationDto v : locations) {
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
